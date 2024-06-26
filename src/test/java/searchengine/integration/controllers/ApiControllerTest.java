package searchengine.integration.controllers;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import searchengine.integration.tools.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@DisplayName("\"ApiController\" integration tests")
class ApiControllerTest {
    private final MockMvc mockMvc;

    @Test
    @DisplayName("\"startIndexing\" good request")
    void startIndexing() throws Exception {
        mockMvc.perform(get("/api/startIndexing"))
                .andExpect(status().isOk());
    }
}