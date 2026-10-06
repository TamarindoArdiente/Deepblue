package com.deepblue.rescue.serviceTest;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    private TreatmentServiceImpl service;

    private RescueCase rescueCase;
    private Animal animal;
    private Specialist specialist;

    @BeforeEach
    void setUp() {
        service = new TreatmentServiceImpl(animalRepository, specialistRepository, treatmentRepository, mapper);

        rescueCase = new RescueCase("RES-001", LocalDate.of(2026, 8, 20), "Bahia Concha",
                RescueStatus.IN_REHABILITATION);
        animal = new Animal("AN-001", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);

        specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org");
    }

    private CreateTreatmentRequest validRequest() {
        return new CreateTreatmentRequest(
                "AN-001", "SPEC-001", LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE, "Cleaning of left front flipper injury.");
    }

    @Test
    void register_solicitudValida_guardaTratamiento() {
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));
        when(treatmentRepository.save(any(Treatment.class))).thenAnswer(inv -> inv.getArgument(0));
       when(mapper.toResponse(any(Treatment.class))).thenAnswer(inv -> {
    Treatment t = inv.getArgument(0);

    return new TreatmentResponse(
            1L,
            t.getAnimal().getAnimalCode(),
            t.getSpecialist().getProfessionalCode(),
            t.getPerformedAt(),
            t.getType(),
            t.getDescription()
    );
});

        TreatmentResponse result = service.register(validRequest());

        assertThat(result.animalCode()).isEqualTo("AN-001");
        assertThat(result.specialistCode()).isEqualTo("SPEC-001");
        verify(treatmentRepository).save(any(Treatment.class));
    }

    @Test
    void register_animalInexistente_lanzaResourceNotFoundException() {
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void register_specialistInexistente_lanzaResourceNotFoundException() {
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void register_specialistInactivo_lanzaBusinessRuleException() {
        specialist.setActive(false);
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(BusinessRuleException.class);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void register_casoReleased_lanzaBusinessRuleException() {
        rescueCase.setStatus(RescueStatus.RELEASED);
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(BusinessRuleException.class);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void register_casoClosed_lanzaBusinessRuleException() {
        rescueCase.setStatus(RescueStatus.CLOSED);
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(BusinessRuleException.class);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void register_fechaAnteriorAlRescate_lanzaBusinessRuleException() {
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001", "SPEC-001", LocalDateTime.of(2026, 8, 15, 9, 0),
                TreatmentType.OBSERVATION, "Too early check-up attempt.");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void findByAnimalCode_retornaTratamientosOrdenados() {
        when(treatmentRepository.findByAnimalAnimalCodeOrderByPerformedAtAsc("AN-001"))
                .thenReturn(java.util.List.of());
        when(mapper.toResponseList(java.util.List.of())).thenReturn(java.util.List.of());

        var result = service.findByAnimalCode("AN-001");

        assertThat(result).isEmpty();
        verify(treatmentRepository).findByAnimalAnimalCodeOrderByPerformedAtAsc("AN-001");
    }
}
