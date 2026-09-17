package com.deepblue.rescue.serviceTest;

import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.impl.RescueCaseServiceImpl;
import com.deepblue.rescue.mapper.RescueCaseMapper;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import org.junit.jupiter.api.Test;


@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper;

    @InjectMocks
    private RescueCaseServiceImpl service;
}

@Test
void shouldFindRescueCaseByCode() {

    RescueCase rescueCase = /* crear objeto */;

    RescueCaseResponse response =
            /* crear response */;

    when(
        repository.findByCaseCode("RES-001")
    ).thenReturn(
        Optional.of(rescueCase)
    );

    when(
        mapper.toResponse(rescueCase)
    ).thenReturn(response);

    RescueCaseResponse result =
            service.findByCode("RES-001");

    assertThat(result)
            .isEqualTo(response);

    verify(repository)
            .findByCaseCode("RES-001");

    verify(mapper)
            .toResponse(rescueCase);
}

