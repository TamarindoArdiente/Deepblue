package com.deepblue.rescue.serviceTest;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.RescueCaseMapper;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.impl.RescueCaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper;

    @InjectMocks
    private RescueCaseServiceImpl service;

    private RescueCase newCase(RescueStatus status) {
        return new RescueCase("RES-001", LocalDate.of(2026, 8, 20), "Bahia Concha", status);
    }

    @Test
    void shouldFindRescueCaseByCode() {
        RescueCase rescueCase = newCase(RescueStatus.IN_REHABILITATION);
        RescueCaseResponse response = new RescueCaseResponse(
                1L, "RES-001", rescueCase.getRescueDate(), "Bahia Concha",
                RescueStatus.IN_REHABILITATION, "DB-CAR", "AN-001");

        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        RescueCaseResponse result = service.findByCode("RES-001");

        assertThat(result).isEqualTo(response);
        verify(repository).findByCaseCode("RES-001");
        verify(mapper).toResponse(rescueCase);
    }

    @Test
    void shouldThrowResourceNotFoundWhenCaseDoesNotExist() {
        when(repository.findByCaseCode("RES-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("RES-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("RES-999");

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnCasesFilteredByStatus() {
        RescueCase rescueCase = newCase(RescueStatus.ADMITTED);
        RescueCaseResponse response = new RescueCaseResponse(
                1L, "RES-001", rescueCase.getRescueDate(), "Bahia Concha",
                RescueStatus.ADMITTED, "DB-CAR", "AN-001");

        when(repository.findByStatusOrderByRescueDateAsc(RescueStatus.ADMITTED))
                .thenReturn(List.of(rescueCase));
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        List<RescueCaseResponse> result = service.findByStatus(RescueStatus.ADMITTED);

        assertThat(result).containsExactly(response);
    }

    @Test
    void shouldChangeStatusWhenTransitionIsValid() {
        RescueCase rescueCase = newCase(RescueStatus.ADMITTED);
        RescueCaseResponse response = new RescueCaseResponse(
                1L, "RES-001", rescueCase.getRescueDate(), "Bahia Concha",
                RescueStatus.UNDER_EVALUATION, "DB-CAR", "AN-001");

        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));
        when(repository.save(rescueCase)).thenReturn(rescueCase);
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.UNDER_EVALUATION);

        RescueCaseResponse result = service.changeStatus("RES-001", request);

        assertThat(result.status()).isEqualTo(RescueStatus.UNDER_EVALUATION);
        assertThat(rescueCase.getStatus()).isEqualTo(RescueStatus.UNDER_EVALUATION);
        verify(repository).save(rescueCase);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenTransitionIsInvalid() {
        RescueCase rescueCase = newCase(RescueStatus.ADMITTED);
        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));

        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.READY_FOR_RELEASE);

        assertThatThrownBy(() -> service.changeStatus("RES-001", request))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenTransitioningFromReleased() {
        RescueCase rescueCase = newCase(RescueStatus.RELEASED);
        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));

        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.IN_REHABILITATION);

        assertThatThrownBy(() -> service.changeStatus("RES-001", request))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenChangingStatusOfUnknownCase() {
        when(repository.findByCaseCode("RES-999")).thenReturn(Optional.empty());
        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.UNDER_EVALUATION);

        assertThatThrownBy(() -> service.changeStatus("RES-999", request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }
}
