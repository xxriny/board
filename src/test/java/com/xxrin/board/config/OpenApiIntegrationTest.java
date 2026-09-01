package com.xxrin.board.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import com.xxrin.board.controller.OpenApiController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiIntegrationTest {

    @Test
    void specificationContainsNineOperationsAndBoardCommentTags() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/openapi.json")) {
            assertThat(input).isNotNull();
            JsonNode root = new ObjectMapper().readTree(input);
            assertThat(root.path("openapi").asText()).startsWith("3.");
            assertThat(root.path("servers")).hasSize(1);
            assertThat(root.path("servers").get(0).path("url").asText()).isEqualTo("/board");
            long operations = StreamSupport.stream(root.path("paths").spliterator(), false)
                    .flatMap(path -> StreamSupport.stream(path.spliterator(), false))
                    .filter(node -> node.has("operationId"))
                    .count();
            assertThat(operations).isEqualTo(9);
            assertThat(root.path("tags")).extracting(node -> node.path("name").asText())
                    .containsExactly("Board", "Comment");
        }
    }

    @Test
    void servesOpenApiJsonAndSwaggerUiEntryPoint() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OpenApiController()).build();

        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
        String html = mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("SwaggerUIBundle", "../v3/api-docs");
    }

    @Test
    void webConfigurationSupportsResourceAndHtmlResponses() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        new WebConfig().configureMessageConverters(converters);

        assertThat(converters).anyMatch(ResourceHttpMessageConverter.class::isInstance);
        assertThat(converters).anyMatch(StringHttpMessageConverter.class::isInstance);
    }
}
