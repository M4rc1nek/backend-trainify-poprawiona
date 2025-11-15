package com.trainify.trainifybackend.training.dto;

import java.util.List;

public record TrainingPlanDTO (
        String name,
        List<TrainingExerciseDTO> exercises

){}
