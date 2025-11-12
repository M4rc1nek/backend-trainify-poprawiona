package com.trainify.trainifybackend.training.dto;

import com.trainify.trainifybackend.training.model.ExerciseCategory;

public record TrainingExerciseDTO(

        Long id,

        ExerciseCategory exerciseCategory,

        String note,

        int amount,
        int duration

        ) {
}
