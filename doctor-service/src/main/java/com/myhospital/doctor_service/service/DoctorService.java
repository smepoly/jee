package com.myhospital.doctor_service.service;

import com.myhospital.doctor_service.model.Doctor;
import com.myhospital.doctor_service.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public List<Doctor> getAllDoctors() {
        return repository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return repository.findById(id);
    }

    public Doctor createDoctor(Doctor doctor) {
        return repository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor doctor) {
        doctor.setId(id);
        return repository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        repository.deleteById(id);
    }
}