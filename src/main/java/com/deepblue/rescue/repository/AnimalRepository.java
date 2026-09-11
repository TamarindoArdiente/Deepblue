package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // Consulta A: buscar animal por animalCode
    Optional<Animal> findByAnimalCode(String animalCode);

    // Consulta B: animales cuyo commonName contenga determinado texto, ignorando mayúsculas/minúsculas
    List<Animal> findByCommonNameContainingIgnoreCase(String commonName);

    // Paso 35: animales cuyo caso de rescate tenga determinado estado
    // Animal -> rescueCase -> status
    List<Animal> findByRescueCaseStatus(RescueStatus status);

    // Paso 36: animales pertenecientes a un centro determinado
    // Animal -> RescueCase -> RescueCenter -> code
    List<Animal> findByRescueCaseRescueCenterCode(String centerCode);

    // PARTE XIII — Reto sin guía.
    // Animales en determinado status de rehabilitación que hayan recibido al menos
    // un tratamiento realizado por un especialista con determinada experiencia.
    //
    // Se eligió @Query + JPQL (y no un Query Method) porque la consulta combina dos
    // caminos de navegación distintos sobre relaciones (rescueCase.status por un lado,
    // treatments.specialist.expertiseAreas.name por el otro), lo que un nombre de
    // Query Method no podría expresar con claridad, y además requiere DISTINCT para
    // evitar animales duplicados cuando tienen varios tratamientos que cumplen la condición.
    @Query("""
            select distinct a
            from Animal a
            join a.rescueCase rc
            join a.treatments t
            join t.specialist s
            join s.expertiseAreas e
            where rc.status = :status
            and lower(e.name) = lower(:expertiseName)
            """)
    List<Animal> findInStatusTreatedBySpecialistWithExpertise(@Param("status") RescueStatus status,
                                                                @Param("expertiseName") String expertiseName);
}
