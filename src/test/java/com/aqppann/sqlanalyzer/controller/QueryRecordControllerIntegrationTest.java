package com.aqppann.sqlanalyzer.controller;

import com.aqppann.sqlanalyzer.dto.QueryRecordRequestDto;
import com.aqppann.sqlanalyzer.repository.QueryRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class QueryRecordControllerIntegrationTest{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private QueryRecordRepository repository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateQueryRecordAndReturnStatus201() throws Exception {
        QueryRecordRequestDto request = new QueryRecordRequestDto();
        request.setSqlText("SELECT * FROM user");
        request.setExecutionTimeMs(1500L);
        request.setDatabaseName("production");

        mockMvc.perform(post("/api/v1/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.status").value("SLOW"))
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturnStatus400_whenSqlTextIsBlank() throws Exception {
        QueryRecordRequestDto request = new QueryRecordRequestDto();
        request.setSqlText("");
        request.setExecutionTimeMs(1500L);

        mockMvc.perform(post("/api/v1/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void shouldReturnStatus404_whenQueryRecordNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/queries/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldReturnAllQueryRecords() throws Exception {
        QueryRecordRequestDto request = new QueryRecordRequestDto();
        request.setSqlText("SELECT id FROM orders");
        request.setExecutionTimeMs(500L);

        mockMvc.perform(post("/api/v1/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldDeleteQueryRecord() throws Exception {
        QueryRecordRequestDto request = new QueryRecordRequestDto();
        request.setSqlText("SELECT id FROM users");
        request.setExecutionTimeMs(500L);

        String response = mockMvc.perform(post("/api/v1/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        mockMvc.perform(delete("/api/v1/queries/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnStatistics() throws Exception {
        QueryRecordRequestDto request = new QueryRecordRequestDto();
        request.setSqlText("SELECT * FROM users");
        request.setExecutionTimeMs(6000L);

        mockMvc.perform(post("/api/v1/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/queries/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalCount").value(1));
    }
}