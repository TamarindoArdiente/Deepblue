package com.deepblue.rescue.dto.request;

import com.deepblue.rescue.domain.TreatmentType;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public record CreateTreatmentRequest(

        @NotBlank(message = "Animal code is required")
        String animalCode,

        @NotBlank(message = "Specialist code is required")
        String specialistCode,

        @NotNull(message = "Performed date is required")
        LocalDateTime performedAt,

        @NotNull(message = "Treatment type is required")
        TreatmentType type,

        @NotBlank(message = "Description is required")
        String description
) {
}
