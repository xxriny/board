package com.xxrin.board.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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
    void generatedSpecificationDocumentsEveryOperation() throws Exception {
        String body = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("openapi").asText()).startsWith("3.");
        assertThat(root.path("paths").has("/api/boards")).isTrue();
        assertThat(root.path("paths").has("/api/auth/signup")).isTrue();
        assertThat(root.path("tags")).extracting(node -> node.path("name").asText())
                .contains("Auth", "Member", "Board", "Comment");
        assertThat(StreamSupport.stream(root.path("paths").spliterator(), false)
                .flatMap(path -> StreamSupport.stream(path.spliterator(), false))
                .filter(operation -> operation.has("operationId"))
                .toList())
                .allSatisfy(operation -> {
                    assertThat(operation.path("summary").asText()).isNotBlank();
                    assertThat(operation.path("description").asText()).isNotBlank();
                });
    }

    @Test
    void servesSwaggerUiEntryPoint() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAnonymousUnknownPath() throws Exception {
        mvc.perform(get("/not-found"))
                .andExpect(status().isUnauthorized());
    }

}
