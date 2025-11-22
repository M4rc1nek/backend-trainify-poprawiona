package com.trainify.trainifybackend.healthcore.controller;


import com.trainify.trainifybackend.healthcore.dto.UserHealthMetricsDTO;
import com.trainify.trainifybackend.healthcore.model.UserHealthMetrics;
import com.trainify.trainifybackend.healthcore.service.HealthCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserHealthMetricsController {

    private final HealthCalculatorService healthCalculatorService;

    @PostMapping("/addBMI/{userId}")
    public ResponseEntity<UserHealthMetricsDTO> addBMI(@RequestBody @Valid UserHealthMetricsDTO dto, @PathVariable Long userId) {
        UserHealthMetrics metrics = healthCalculatorService.buildBMI(dto, userId);
        return ResponseEntity.ok(healthCalculatorService.mapToDTO(metrics));
    }


    @PostMapping("/addBMR/{userId}")
    public ResponseEntity<UserHealthMetricsDTO> addBMR(@RequestBody @Valid UserHealthMetricsDTO dto, @PathVariable Long userId) {
        UserHealthMetrics metrics = healthCalculatorService.buildBMR(dto, userId);
        return ResponseEntity.ok(healthCalculatorService.mapToDTO(metrics));
    }


    @PostMapping("/addTDEE/{userId}")
    public ResponseEntity<UserHealthMetricsDTO> addTDEE(@RequestBody @Valid UserHealthMetricsDTO dto, @PathVariable Long userId){
        UserHealthMetrics metrics = healthCalculatorService.buildTDEE(dto, userId);
        return ResponseEntity.ok(healthCalculatorService.mapToDTO(metrics));
    }

    @GetMapping("/getBMI/{userId}")
    public ResponseEntity<Double> getBMI(@PathVariable Long userId) {
        return ResponseEntity.ok(healthCalculatorService.getBMI(userId));
    }


    @GetMapping("/getBMR/{userId}")
    public ResponseEntity<Double> getBMR(@PathVariable Long userId) {
        return ResponseEntity.ok(healthCalculatorService.getBMR(userId));
    }


    @GetMapping("/getTDEE/{userId}")
    public ResponseEntity<Double> getTDEE(@PathVariable Long userId) {
        return ResponseEntity.ok(healthCalculatorService.getTDEE(userId));
    }


}

