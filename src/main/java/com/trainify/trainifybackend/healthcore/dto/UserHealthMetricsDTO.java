package com.trainify.trainifybackend.healthcore.dto;

import com.trainify.trainifybackend.healthcore.model.ActivityLevel;
import com.trainify.trainifybackend.healthcore.model.BmiFeedback;
import com.trainify.trainifybackend.healthcore.model.GenderType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record UserHealthMetricsDTO(

        Long id,


        ActivityLevel activityLevel,
        GenderType genderType,

        int age,
        @DecimalMin("1.0") @DecimalMax("250.0") double height, // cm
        @DecimalMin("30.0") @DecimalMax("300.0") double weight, // kg

        double BMI,
        double BMR,
        double TDEE,
        BmiFeedback bmiFeedback

) {
}
