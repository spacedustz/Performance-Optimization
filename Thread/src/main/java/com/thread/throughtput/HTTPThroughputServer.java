package com.thread.throughtput;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HTTPThroughputServer {
    private static final String INPUT_FILE = "throughput/war_and_peace.txt";
    private static final int NUMBER_OF_THREAD = 2;

    public static void main(String[] args) throws IOException {
        Resource bookResource = new ClassPathResource(INPUT_FILE);
        String bookText = new String(Files.readAllBytes(Paths.get(bookResource.getURI())));

        startServer(bookText);
    }

    /* Create & Start HTTP Server */
    public static void startServer(String text) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/search", new WordCountHandler(text));

        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREAD);
        server.setExecutor(executor);
        server.start();
    }

    /* HTTP Request Handler */
    @AllArgsConstructor
    private static class WordCountHandler implements HttpHandler {
        private String text;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery(); // word=xxxx
            String[] keyValue = query.split("="); // word / xxxx
            String action = keyValue[0]; // word
            String word = keyValue[1]; // xxxx

            // Request Param의 Key가 word가 아니면 400 Error
            if (!action.equals("word")) {
                exchange.sendResponseHeaders(400, 0);
                return;
            }

            long count = countWord(word);

            // Response를 주고 OutputStream을 닫아줌
            byte[] response = Long.toString(count).getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response);
            outputStream.close();
        }

        /* 책에서 나오는 단어의 개수를 세는 함수 */
        private long countWord(String word) {
            long count = 0;
            int index = 0;

            while (index >= 0) {
                index = text.indexOf(word, index);

                // index가 양수면 단어를 찾은 것임
                if (index >= 0) {
                    count++;
                    index++;
                }
            }

            // index가 음수면 더이상 찾을 단어가 없으니 count를 반환하면서 return
            return count;
        }
    }
}
