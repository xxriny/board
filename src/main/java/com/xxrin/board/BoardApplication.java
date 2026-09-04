package com.xxrin.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot 기반 게시판 API 애플리케이션 진입점이다. */
@SpringBootApplication
public class BoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }
}
