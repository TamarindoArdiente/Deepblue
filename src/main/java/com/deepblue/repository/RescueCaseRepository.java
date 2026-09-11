package com.deepblue.repository;

import com.deepblue.domain.RescueCase;
import com.deepblue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {

   
    Optional<RescueCase> findByCaseCode(String caseCode);

  
    List<RescueCase> findByStatusOrderByRescueDateAsc(RescueStatus status);

    List<RescueCase> findByRescueCenterCode(String code);


    List<RescueCase> findByRescueDateAfterOrderByRescueDateDesc(LocalDate date);

   
    boolean existsByCaseCode(String caseCode);
}