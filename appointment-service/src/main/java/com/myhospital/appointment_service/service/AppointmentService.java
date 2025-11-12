package com.myhospital.appointment_service.service;

import com.myhospital.appointment_service.feign.DoctorClient;
import com.myhospital.appointment_service.feign.PatientClient;
import com.myhospital.appointment_service.model.Appointment;
import com.myhospital.appointment_service.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final DoctorClient doctorClient;
    private final PatientClient patientClient;

    public AppointmentService(AppointmentRepository repository, DoctorClient doctorClient, PatientClient patientClient) {
        this.repository = repository;
        this.doctorClient = doctorClient;
        this.patientClient = patientClient;
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return repository.findById(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        // Optional: Check if doctor and patient exist via Feign
        doctorClient.getDoctorById(appointment.getDoctorId());
        patientClient.getPatientById(appointment.getPatientId());
        return repository.save(appointment);
    }

    public Appointment updateAppointment(Long id, Appointment appointment) {
        appointment.setId(id);
        return repository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        repository.deleteById(id);
    }
}
