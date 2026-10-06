package com.deepblue.rescue.serviceTest;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.impl.AnimalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AnimalMapper mapper;

    @InjectMocks
    private AnimalServiceImpl service;

    private Animal animalWithStatus(RescueStatus status) {
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.of(2026, 8, 20), "Bahia Concha", status);
        Animal animal = new Animal("AN-001", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);
        return animal;
    }

    @Test
    void findByCode_animalExistente_retornaDto() {
        Animal animal = animalWithStatus(RescueStatus.IN_REHABILITATION);
        AnimalResponse response = new AnimalResponse(
                1L, "AN-001", "Green Sea Turtle", "Chelonia mydas",
                AnimalSex.FEMALE, "RES-001", RescueStatus.IN_REHABILITATION);

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(mapper.toResponse(animal)).thenReturn(response);

        AnimalResponse result = service.findByCode("AN-001");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findByCode_animalInexistente_lanzaResourceNotFoundException() {
        when(animalRepository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("AN-999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAnimalsInRehabilitation_retornaSoloEsosAnimales() {
        Animal animal = animalWithStatus(RescueStatus.IN_REHABILITATION);
        List<AnimalResponse> expected = List.of(new AnimalResponse(
                1L, "AN-001", "Green Sea Turtle", "Chelonia mydas",
                AnimalSex.FEMALE, "RES-001", RescueStatus.IN_REHABILITATION));

        when(animalRepository.findByRescueCaseStatus(RescueStatus.IN_REHABILITATION))
                .thenReturn(List.of(animal));
        when(mapper.toResponseList(List.of(animal))).thenReturn(expected);

        List<AnimalResponse> result = service.findAnimalsInRehabilitation();

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = RescueStatus.class, names = {"UNDER_EVALUATION", "IN_REHABILITATION"})
    void canReceiveTreatment_statusElegible_retornaTrue(RescueStatus status) {
        Animal animal = animalWithStatus(status);
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean result = service.canReceiveTreatment("AN-001");

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = RescueStatus.class, names = {"ADMITTED", "READY_FOR_RELEASE", "RELEASED", "CLOSED"})
    void canReceiveTreatment_statusNoElegible_retornaFalse(RescueStatus status) {
        Animal animal = animalWithStatus(status);
        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean result = service.canReceiveTreatment("AN-001");

        assertThat(result).isFalse();
    }

    @Test
    void canReceiveTreatment_animalInexistente_lanzaResourceNotFoundException() {
        when(animalRepository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.canReceiveTreatment("AN-999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
