package com.hardware.serviceplatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String root() {
        return "Hardware Service Platform API is running";
    }

    @GetMapping("/api/test")
    public String test() {
        return "OK";
    }
}