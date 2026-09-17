package com.deepblue.rescue.dto.response;

import com.deepblue.rescue.domain.TreatmentType;

public record TreatmentResponse(

        Long id,

        String animalCode,

        String specialistCode,

        java.time.LocalDateTime performedAt,

        TreatmentType type,

        String description

) {
}
