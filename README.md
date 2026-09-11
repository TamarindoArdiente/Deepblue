# DeepBlue Rescue

## 1. Descripción

**DeepBlue Rescue** es la capa de persistencia de una plataforma para organizaciones dedicadas al
rescate y rehabilitación de fauna marina. El proyecto implementa:

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- Flyway
- PostgreSQL
- Testcontainers

## 2. Modelo de datos utilizado

RescueCenter 1 ───────── N RescueCase
RescueCase   1 ───────── 1 Animal
Animal       1 ───────── 1 MedicalRecord
Specialist   N ───────── M Expertise
Animal       1 ───────── N Treatment
Specialist   1 ───────── N Treatment


Tablas creadas por Flyway:


rescue_centers
rescue_cases
animals
medical_records
specialists
expertise
specialist_expertise 


## 3. Las relaciones JPA

| Relación | Lado propietario (`@JoinColumn`) | Lado inverso (`mappedBy`) |
|---|---|---|
| RescueCenter 1:N RescueCase | `RescueCase.rescueCenter` | `RescueCenter.rescueCases` |
| RescueCase 1:1 Animal | `Animal.rescueCase` | `RescueCase.animal` |
| Animal 1:1 MedicalRecord | `MedicalRecord.animal` | `Animal.medicalRecord` |
| Specialist N:M Expertise | `Specialist.expertiseAreas` (`@JoinTable`) | `Expertise.specialists` |
| Treatment N:1 Animal | `Treatment.animal` | `Animal.treatments` |
| Treatment N:1 Specialist | `Treatment.specialist` | `Specialist.treatments` |

## 4. Como ejecutar la aplicación

Requiere una instancia de PostgreSQL disponible (o variables de entorno `DB_URL`, `DB_USER`,
`DB_PASSWORD`):


mvn spring-boot:run


## 5. Como ejecutar las pruebas

Las pruebas usan Testcontainers y Docker debe estar disponible.

mvn clean test



## 6. Sobre Flyway

Flyway es el único responsable de crear y evolucionar el esquema

Migraciones:


V1__create_schema.sql                     -> crea todas las tablas, PKs, FKs, UNIQUE, CHECK e índices
V2__insert_expertise_catalog.sql          -> inserta el catálogo inicial de Expertise
V3__add_tracking_device_to_animal.sql     -> agrega tracking_device_code (nullable, UNIQUE) a animals


## 7. Explicaciòn Testcontainers

`PersistenceIntegrationTest` levanta un contenedor real de `postgres:18-alpine` mediante
`@Testcontainers` + `@Container` + `@ServiceConnection`, de modo que las pruebas corren contra
PostgreSQL real, validando constraints reales (UNIQUE, FK, CHECK).

## 8. Query Methods implementados


##  RescueCaseRepository

- `RescueCaseRepository.findByCaseCode`
- `RescueCaseRepository.existsByCaseCode`
- `RescueCaseRepository.findByStatusOrderByRescueDateAsc`
- `RescueCaseRepository.findByRescueCenterCode`
- `RescueCaseRepository.findByRescueDateAfterOrderByRescueDateDesc`

---

##  AnimalRepository

- `AnimalRepository.findByAnimalCode`
- `AnimalRepository.findByCommonNameContainingIgnoreCase`
- `AnimalRepository.findByRescueCaseStatus`
- `AnimalRepository.findByRescueCaseRescueCenterCode`

---

##  ExpertiseRepository

- `ExpertiseRepository.findByNameIgnoreCase`

---

##  TreatmentRepository

- `TreatmentRepository.findByAnimalIdOrderByPerformedAtAsc`


## 9. Consultas JPQL implementadas 

```text
SpecialistRepository.findActiveByExpertise
    -> especialistas activos con determinada expertise (JOIN + LOWER + parámetro nombrado)

TreatmentRepository.findByPerformedAtBetween
    -> tratamientos realizados entre dos fechas

TreatmentRepository.findByAnimalRescueCaseRescueCenterCode
    -> tratamientos de animales de un centro (Treatment -> Animal -> RescueCase -> RescueCenter)

TreatmentRepository.findBySpecialistExpertise
    -> tratamientos realizados por especialistas con determinada expertise (N:M)

AnimalRepository.findInStatusTreatedBySpecialistWithExpertise
    -> Reto sin guía: animales en determinado status que recibieron al menos un tratamiento
       de un especialista con determinada expertise (DISTINCT, dos caminos de navegación)
```
