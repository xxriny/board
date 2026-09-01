package com.xxrin.board.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 정적 OpenAPI 명세와 Swagger UI 진입점을 제공한다. */
@RestController
public class OpenApiController {

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource specification() {
        return new ClassPathResource("openapi.json");
    }

    @GetMapping(value = "/swagger-ui/index.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> swaggerUi() {
        return ResponseEntity.ok("""
                <!doctype html><html><head><meta charset="utf-8">
                <title>Board API</title>
                <link rel="stylesheet" href="../webjars/swagger-ui/5.29.5/swagger-ui.css">
                </head><body><div id="swagger-ui"></div>
                <script src="../webjars/swagger-ui/5.29.5/swagger-ui-bundle.js"></script>
                <script>SwaggerUIBundle({url:'../v3/api-docs',dom_id:'#swagger-ui'});</script>
                </body></html>
                """);
    }
}
