package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService service;

    private static final String VALID_BODY = """
            {
                "animalCode": "AN-001",
                "specialistCode": "SPEC-001",
                "performedAt": "2026-08-21T09:00:00",
                "type": "WOUND_CARE",
                "description": "Cleaning and treatment of flipper injury."
            }
            """;

    @Test
    void shouldCreateTreatment() throws Exception {
        TreatmentResponse response = new TreatmentResponse(
                100L, "AN-001", "SPEC-001", LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE, "Cleaning and treatment of flipper injury.");

        when(service.register(any(CreateTreatmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.type").value("WOUND_CARE"));

        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        String invalidBody = """
                {
                    "animalCode": "",
                    "specialistCode": "",
                    "type": null,
                    "description": ""
                }
                """;

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.animalCode").exists())
                .andExpect(jsonPath("$.details.specialistCode").exists())
                .andExpect(jsonPath("$.details.type").exists())
                .andExpect(jsonPath("$.details.description").exists());

        verify(service, never()).register(any());
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(service.register(any(CreateTreatmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Animal not found: AN-999"));

        String body = VALID_BODY.replace("AN-001", "AN-999");

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Animal not found: AN-999"));
    }

    @Test
    void shouldReturn409WhenBusinessRuleIsViolated() throws Exception {
        when(service.register(any(CreateTreatmentRequest.class)))
                .thenThrow(new BusinessRuleException("Released animals cannot receive treatments"));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Released animals cannot receive treatments"));
    }

    @Test
    void shouldReturn400WhenJsonEnumIsInvalid() throws Exception {
        String invalidEnumBody = VALID_BODY.replace("WOUND_CARE", "FLYING");

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEnumBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or invalid JSON request"));

        verify(service, never()).register(any());
    }
}
