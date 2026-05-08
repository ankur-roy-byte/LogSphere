package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.SavedSearchRequest;
import com.ankur.loganalyzer.repository.SavedSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SavedSearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SavedSearchRepository savedSearchRepository;

    @BeforeEach
    void setUp() {
        savedSearchRepository.deleteAll();
    }

    @Test
    void createListRunAndDeleteSavedSearch() throws Exception {
        SavedSearchRequest request = SavedSearchRequest.builder()
                .name("API errors")
                .description("Errors from the API service")
                .serviceName("api-service")
                .level("error")
                .keyword("timeout")
                .host("api-01")
                .build();

        String content = mockMvc.perform(post("/api/saved-searches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("API errors")))
                .andExpect(jsonPath("$.level", is("ERROR")))
                .andExpect(jsonPath("$.createdBy", is("system")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(content).get("id").asLong();

        mockMvc.perform(get("/api/saved-searches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(id.intValue())));

        mockMvc.perform(get("/api/saved-searches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyword", is("timeout")));

        mockMvc.perform(get("/api/saved-searches/{id}/logs", id)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(delete("/api/saved-searches/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/saved-searches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
