package com.myhospital.patient_service.service;
import com.myhospital.patient_service.model.Patient;
import com.myhospital.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Patient save(Patient patient) {
        return repository.save(patient);
    }

    public List<Patient> findAll() {
        return repository.findAll();
    }

    public Optional<Patient> findById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}