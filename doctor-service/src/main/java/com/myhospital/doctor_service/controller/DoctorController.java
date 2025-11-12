package com.myhospital.doctor_service.controller;

import com.myhospital.doctor_service.model.Doctor;
import com.myhospital.doctor_service.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return service.getAllDoctors();
    }

    @GetMapping("/{id}")
    public Doctor getDoctor(@PathVariable Long id) {
        return service.getDoctorById(id).orElse(null);
    }

    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return service.createDoctor(doctor);
    }

    @PutMapping("/{id}")
    public Doctor updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        return service.updateDoctor(id, doctor);
    }

    @DeleteMapping("/{id}")
    public void deleteDoctor(@PathVariable Long id) {
        service.deleteDoctor(id);
    }
}
