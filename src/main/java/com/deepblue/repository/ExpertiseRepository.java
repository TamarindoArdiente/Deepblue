package com.deepblue.repository;

import com.deepblue.domain.Expertise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExpertiseRepository extends JpaRepository<Expertise, Long> {

    // Query Method: buscar por nombre ignorando mayúsculas/minúsculas
    Optional<Expertise> findByNameIgnoreCase(String name);
}