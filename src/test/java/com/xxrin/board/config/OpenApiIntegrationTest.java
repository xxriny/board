package com.xxrin.board.config;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:openapi;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatedSpecificationContainsNineOperationsAndBoardCommentTags() throws Exception {
        String body = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("openapi").asText()).startsWith("3.");
        long operations = StreamSupport.stream(root.path("paths").spliterator(), false)
                .flatMap(path -> StreamSupport.stream(path.spliterator(), false))
                .filter(node -> node.has("operationId"))
                .count();
        assertThat(operations).isEqualTo(9);
        assertThat(root.path("tags")).extracting(node -> node.path("name").asText())
                .contains("Board", "Comment");
    }

    @Test
    void servesSwaggerUiEntryPoint() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void returnsNotFoundForUnknownPath() throws Exception {
        mvc.perform(get("/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEndpointsRejectBlankPasswordBody() throws Exception {
        String body = "{\"password\":\"\"}";

        mvc.perform(delete("/api/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/boards/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
