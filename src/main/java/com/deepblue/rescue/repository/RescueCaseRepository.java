package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {

    // Consulta A: buscar un caso por caseCode
    Optional<RescueCase> findByCaseCode(String caseCode);

    // Utilizado en el reto integrador: ¿existe el caso RES-2026-100?
    boolean existsByCaseCode(String caseCode);

    // Consulta B: todos los casos según status, ordenados por rescueDate ASC
    List<RescueCase> findByStatusOrderByRescueDateAsc(RescueStatus status);

    // Consulta C: casos pertenecientes a un centro determinado, a partir de rescueCenter.code
    List<RescueCase> findByRescueCenterCode(String code);

    // Query Method con fechas: casos posteriores a determinada fecha, del más reciente al más antiguo
    List<RescueCase> findByRescueDateAfterOrderByRescueDateDesc(LocalDate rescueDate);
}
