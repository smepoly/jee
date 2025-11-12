package com.myhospital.appointment_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/patients/{id}")
    PatientResponse getPatientById(@PathVariable("id") Long id);

    class PatientResponse {
        private Long id;
        private String name;
        private String email;

        // getters and setters
    }
}
