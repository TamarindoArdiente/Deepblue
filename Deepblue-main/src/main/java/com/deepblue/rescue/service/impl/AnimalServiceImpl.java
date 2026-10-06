package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.AnimalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AnimalServiceImpl implements AnimalService {

    private static final Set<RescueStatus> ELIGIBLE_FOR_TREATMENT =
            Set.of(RescueStatus.UNDER_EVALUATION, RescueStatus.IN_REHABILITATION);

    private final AnimalRepository animalRepository;
    private final AnimalMapper mapper;

    public AnimalServiceImpl(AnimalRepository animalRepository, AnimalMapper mapper) {
        this.animalRepository = animalRepository;
        this.mapper = mapper;
    }

    @Override
    public AnimalResponse findByCode(String animalCode) {
        return animalRepository.findByAnimalCode(animalCode)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + animalCode));
    }

    @Override
    public List<AnimalResponse> findAnimalsInRehabilitation() {
        return mapper.toResponseList(
                animalRepository.findByRescueCaseStatus(RescueStatus.IN_REHABILITATION));
    }

    @Override
    public boolean canReceiveTreatment(String animalCode) {
        Animal animal = animalRepository.findByAnimalCode(animalCode)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + animalCode));

        return ELIGIBLE_FOR_TREATMENT.contains(animal.getRescueCase().getStatus());
    }
}
