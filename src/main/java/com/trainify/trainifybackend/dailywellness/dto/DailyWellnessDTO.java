package com.trainify.trainifybackend.dailywellness.dto;

import com.trainify.trainifybackend.dailywellness.model.ReadinessLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record DailyWellnessDTO(

        Long id,

        @PastOrPresent LocalDate date,


        @Min(0) @Max(24) double hoursSlept,
        @Min(1) @Max(10) int energyLevel,
        @Min(1) @Max(10) int musclePain,
        @Min(1) @Max(10) int mood,
        @Min(1) @Max(10) int motivation,

        int readinessScore,
        ReadinessLevel readinessLevel,
        String recommendation
) {
}
