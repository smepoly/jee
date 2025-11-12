package com.myhospital.doctor_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    private String name;
    private String specialty;
    private String email;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
