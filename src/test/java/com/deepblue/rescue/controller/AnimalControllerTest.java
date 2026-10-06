package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.AnimalService;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimalController.class)
@Import(GlobalExceptionHandler.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private TreatmentService treatmentService;

    private AnimalResponse sampleAnimal() {
        return new AnimalResponse(
                1L, "AN-001", "Green Sea Turtle", "Chelonia mydas",
                AnimalSex.FEMALE, "RES-001", RescueStatus.IN_REHABILITATION);
    }

    @Test
    void shouldReturnAnimalByCode() throws Exception {
        when(animalService.findByCode("AN-001")).thenReturn(sampleAnimal());

        mockMvc.perform(get("/api/animals/{code}", "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.commonName").value("Green Sea Turtle"));

        verify(animalService).findByCode("AN-001");
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(animalService.findByCode("AN-999"))
                .thenThrow(new ResourceNotFoundException("Animal not found: AN-999"));

        mockMvc.perform(get("/api/animals/{code}", "AN-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Animal not found: AN-999"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void shouldReturnAnimalsInRehabilitation() throws Exception {
        when(animalService.findAnimalsInRehabilitation()).thenReturn(List.of(sampleAnimal(), sampleAnimal()));

        mockMvc.perform(get("/api/animals/in-rehabilitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(animalService).findAnimalsInRehabilitation();
    }

    @Test
    void shouldReturnAnimalTreatments() throws Exception {
        TreatmentResponse treatment = new TreatmentResponse(
                1L, "AN-001", "SPEC-001", LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE, "Cleaning");

        when(treatmentService.findByAnimalCode("AN-001")).thenReturn(List.of(treatment));

        mockMvc.perform(get("/api/animals/{code}/treatments", "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("WOUND_CARE"));

        verify(treatmentService).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnTreatmentEligibility() throws Exception {
        when(animalService.canReceiveTreatment("AN-001")).thenReturn(true);

        mockMvc.perform(get("/api/animals/{code}/treatment-eligibility", "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.eligible").value(true));

        verify(animalService).canReceiveTreatment("AN-001");
    }

    @Test
    void shouldReturn404ForEligibilityWhenAnimalDoesNotExist() throws Exception {
        when(animalService.canReceiveTreatment("AN-999"))
                .thenThrow(new ResourceNotFoundException("Animal not found: AN-999"));

        mockMvc.perform(get("/api/animals/{code}/treatment-eligibility", "AN-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
