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
@ActiveProfiles("test")
class QueryRecordControllerIntegrationTest {
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
        QueryRecordRequestDto request = QueryRecordRequestDto.builder()
                .sqlText("SELECT * FROM user")
                .executionTimeMs(1500L)
                .databaseName("production")
                .build();

        mockMvc.perform(post("/api/v1/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SLOW"))
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturnStatus400_whenSqlTextIsBlank() throws Exception {
        QueryRecordRequestDto request = QueryRecordRequestDto.builder()
                .sqlText("")
                .executionTimeMs(1500L)
                .build();

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
        QueryRecordRequestDto request = QueryRecordRequestDto.builder()
                .sqlText("SELECT id FROM orders")
                .executionTimeMs(500L)
                .build();

        mockMvc.perform(post("/api/v1/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldDeleteQueryRecord() throws Exception {
        QueryRecordRequestDto request = QueryRecordRequestDto.builder()
                .sqlText("SELECT id FROM users")
                .executionTimeMs(500L)
                .build();

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
        QueryRecordRequestDto request = QueryRecordRequestDto.builder()
                .sqlText("SELECT * FROM users")
                .executionTimeMs(6000L)
                .build();

        mockMvc.perform(post("/api/v1/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/queries/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalCount").value(1));
    }
}