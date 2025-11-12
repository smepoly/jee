package com.myhospital.appointment_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorClient {

    @GetMapping("/doctors/{id}")
    DoctorResponse getDoctorById(@PathVariable("id") Long id);

    class DoctorResponse {
        private Long id;
        private String name;
        private String specialty;
        private String email;

        // getters and setters
    }
}
