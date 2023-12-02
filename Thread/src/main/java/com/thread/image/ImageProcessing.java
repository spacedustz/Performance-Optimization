package com.thread.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageProcessing {
    public static final String SOURCE_IMG = "img/Flowers.png";
    public static final String DESTINATION_IMG = "Thread/src/main/resources/img/Purple-Flowers.png";

    public static void main(String[] args) throws IOException {
        Resource originalResource = new ClassPathResource(SOURCE_IMG);
        BufferedImage originalImage = ImageIO.read((originalResource.getFile()));
        BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB // Color Space
        );

        long startTime = System.currentTimeMillis();

        recolorMultiThread(originalImage, resultImage, 3);

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        File outputResult = new File(DESTINATION_IMG); // 파일 생성
        outputResult.getParentFile().mkdirs();
        ImageIO.write(resultImage, "png", outputResult); // 파일 Write

        log.info("총 작업에 걸린 시간 : {}", duration);
    }

    /* 원본, 결과 이미지 픽셀의 x,y 좌표를 표현하는 BufferedImage 객체를 가짐 */
    public static void recolorPixel(BufferedImage original, BufferedImage result, int x, int y) {
        int originalRGB = original.getRGB(x, y);

        int red = getRed(originalRGB);
        int green = getGreen(originalRGB);
        int blue = getBlue(originalRGB);

        int newRed, newGreen, newBlue;
        /**
         * 원본 이미지의 RGB가 회색 계열이라면 색상들의 값을 올리거나 내려줌
         * 이때, 음수가 되지 않도록 Math.max(), 255를 넘어가지 않도록 Math.min()을 이용해줌
         */
        if (isShadeOfGray(red, green, blue)) {
            newRed = Math.min(255, red + 10);
            newGreen = Math.max(0, green - 80);
            newBlue = Math.max(0, blue - 20);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }

        int resultRGB = createRGBFromColors(newRed, newGreen, newBlue);
        setRGB(result, x, y, resultRGB);
    }

    /* BufferedImage에 rgb를 설정 */
    public static void setRGB(BufferedImage image, int x, int y, int rgb) {
        image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
    }

    /* 지정된 영역 안의 모든 픽셀의 색상을 재설정 */
    public static void recolorImage(BufferedImage original,
                               BufferedImage result,
                               int leftCorner,
                               int topCorner,
                               int width,
                               int height) {
        for (int x = leftCorner; x < leftCorner + width && x < original.getWidth(); x++) {
            for (int y = topCorner; y < topCorner + height && y < original.getHeight(); y++) {
                recolorPixel(original, result, x, y);
            }
        }
    }

    /* 전체 이미지에 대해 recolorImage()를 호출해 이미지를 단일 스레드로 색칠 */
    public static void recolorSingleThread(BufferedImage original, BufferedImage result) {
        recolorImage(original, result, 0, 0, original.getWidth(), original.getHeight());
    }

    /* 멀티 스레딩 처리 */
    public static void recolorMultiThread(BufferedImage original, BufferedImage result, int numberOfThreads) {
        List<Thread> threadList = new ArrayList<>();
        int width = original.getWidth();
        int height = original.getHeight() / numberOfThreads;

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadMultiplier = i;

            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = height * threadMultiplier;

                recolorImage(original, result, leftCorner, topCorner, width, height);
            });

            threadList.add(thread);
        }

        for (Thread thread : threadList) {
            thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("Thread Interrupted");
            }
        }
    }

    /* ========== Utils ========== */

    /* 픽셀의 특정 색상 값을 취하고 픽셀에 넣을 회색을 결정 */
    /* 빨강, 초록, 파랑 성분 간의 차이를 절대값으로 계산하고, 각 차이가 30 이하인지 확인 */
    public static boolean isShadeOfGray(int red, int green, int blue) {
        return Math.abs(red - green) < 30 && Math.abs(red - blue) < 30 && Math.abs(green - blue) < 30;
    }

    /* RGB 개별 값을 합쳐서 픽셀에 RGB 값을 넣어주는 함수 */
    public static int createRGBFromColors(int red, int green, int blue) {
        int rgb = 0;

        rgb |= 0xFF000000; // A
        rgb |= red << 16; // R
        rgb |= green << 8; // G
        rgb |= blue; // B

        return rgb;
    }

    /* 특정 색상을 추출하는 함수 */
    public static int getRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }

    public static int getGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }

    public static int getBlue(int rgb) {
        return rgb & 0x000000FF;
    }
}
