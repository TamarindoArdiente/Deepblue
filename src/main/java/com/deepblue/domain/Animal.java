package com.deepblue.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity 
@Table (name = "animals")
public class Animal {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "animalCode", nullable = false, unique = true)
    private String animalCode;

    @Column (name = "commonName", nullable = false)
    private String commonName;

    @Column (name = "scientificName", nullable = false)
    private String scientificName;

    @Column (name = "trackingDeviceCode", unique = true)
    private String trackingDeviceCode;

    @Enumerated (EnumType.STRING)
    @Column (name = "sex", nullable = false)
    private AnimalSex sex;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "rescue_case_id", nullable = false,unique = true)
    private RescueCase rescueCase;

    @OneToOne (mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MedicalRecord medicalRecord;

     @OneToMany(mappedBy = "animal")
    private List<Treatment> treatments = new ArrayList<>();

    public Animal(String animalCode, String commonName, String scientificName, AnimalSex sex) {
        this.animalCode = animalCode;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.sex = sex;
    }

   public void assignMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
        medicalRecord.setAnimal(this);
    }

    public Long getId() {
        return id;
    }

    public String getAnimalCode() {
        return animalCode;
    }

    public void setAnimalCode(String animalCode) {
        this.animalCode = animalCode;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public AnimalSex getSex() {
        return sex;
    }

    public void setSex(AnimalSex sex) {
        this.sex = sex;
    }

    public String getTrackingDeviceCode() {
        return trackingDeviceCode;
    }

    public void setTrackingDeviceCode(String trackingDeviceCode) {
        this.trackingDeviceCode = trackingDeviceCode;
    }

    public RescueCase getRescueCase() {
        return rescueCase;
    }

    public void setRescueCase(RescueCase rescueCase) {
        this.rescueCase = rescueCase;
    }

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public List<Treatment> getTreatments() {
        return treatments;
    }
}   
