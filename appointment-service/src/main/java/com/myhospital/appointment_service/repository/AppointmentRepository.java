package com.myhospital.appointment_service.repository;

import com.myhospital.appointment_service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> { }
