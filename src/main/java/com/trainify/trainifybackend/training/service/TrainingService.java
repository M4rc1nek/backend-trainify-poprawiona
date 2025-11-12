package com.trainify.trainifybackend.training.service;

import com.trainify.trainifybackend.exception.TrainingForUserNotFoundException;
import com.trainify.trainifybackend.exception.UserNotFoundException;
import com.trainify.trainifybackend.training.dto.TrainingDTO;
import com.trainify.trainifybackend.training.dto.TrainingExerciseDTO;
import com.trainify.trainifybackend.training.dto.TrainingStatisticsDTO;
import com.trainify.trainifybackend.training.model.Training;
import com.trainify.trainifybackend.training.model.TrainingExercise;
import com.trainify.trainifybackend.training.repository.TrainingExerciseRepository;
import com.trainify.trainifybackend.training.repository.TrainingRepository;
import com.trainify.trainifybackend.user.model.User;
import com.trainify.trainifybackend.user.repository.UserRepository;
import com.trainify.trainifybackend.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingService {


    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final TrainingExerciseRepository trainingExerciseRepository;
    private final UserService userService;

    @Transactional
    public TrainingDTO addTraining(TrainingDTO trainingDTO) {

        User user = userService.getUserById(trainingDTO.userId())
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika z id " + trainingDTO.userId()));
        Training training = Training.builder()
                .date(trainingDTO.date())
                .createdAt(LocalDateTime.now())
                .userAssigned(user)
                .build();


        //Sprawdzam, czy lista ćwiczeń w dto istnieje i sprawdzam, czy lista nie jest pusta, jeżeli się zgadza tworze ćwiczenia
        if (trainingDTO.exercises() != null && !trainingDTO.exercises().isEmpty()) {
            List<TrainingExercise> exercises = createExerciseMethod(trainingDTO, training);
            training.setExercises(exercises);
            calculateTiS(training);
        }



        trainingRepository.save(training);
        List<TrainingExerciseDTO> getExercise = getExerciseMethod(training);

        return new TrainingDTO(
                training.getId(),
                user.getId(),
                training.getDate(),
                training.getCreatedAt(),
                getExercise
        );

    }

    @Transactional
    public TrainingDTO updateTraining(TrainingDTO trainingDTO, Long trainingId, Long userId) {
        Training existingTraining = trainingRepository.findTrainingByIdAndUserAssigned_Id(trainingId, userId)
                .orElseThrow(() -> new TrainingForUserNotFoundException(
                        "Nie znaleziono treningu o podanym ID dla tego użytkownika"
                ));


        existingTraining.setDate(trainingDTO.date()); // Najpierw zmieniasz date w istniejącym obiekcie, zanim zapiszesz go w repozytorium, to standardowa aktualizacja encji.


        List<TrainingExercise> exercises = createExerciseMethod(trainingDTO, existingTraining);
        existingTraining.setExercises(exercises); //Dzięki CascadeType.ALL i orphanRemoval = true Hibernate sam zajmie się usunięciem starych ćwiczeń i dodaniem nowych.
        calculateTiS(existingTraining);



        trainingRepository.save(existingTraining);

        List<TrainingExerciseDTO> getExercise = getExerciseMethod(existingTraining);

        return new TrainingDTO(
                existingTraining.getId(),
                userId,
                existingTraining.getDate(),
                existingTraining.getCreatedAt(),
                getExercise

        );
    }

    public List<TrainingExercise> createExerciseMethod(TrainingDTO trainingDTO, Training training) {
        return trainingDTO.exercises().stream()
                .map(dto -> TrainingExercise.builder()
                        .id(dto.id())
                        .exerciseCategory(dto.exerciseCategory())
                        .note(dto.note())
                        .amount(dto.amount())
                        .duration(dto.duration())
                        .trainingAssigned(training)
                        .build()
                ).toList();
    }


    public List<TrainingExerciseDTO> getExerciseMethod(Training training) {
        List<TrainingExercise> exercises = training.getExercises();
        if (exercises == null) {
            return Collections.emptyList();
        }
        return exercises.stream().map(
                        exercise -> new TrainingExerciseDTO(
                                exercise.getId(),
                                exercise.getExerciseCategory(),
                                exercise.getNote(),
                                exercise.getAmount(),
                                exercise.getDuration()
                        ))
                .collect(Collectors.toList());
    }


    public void deleteTraining(Long trainingId, Long userId) {
        Training training = trainingRepository.findTrainingByIdAndUserAssigned_Id(trainingId, userId)
                .orElseThrow(() -> new TrainingForUserNotFoundException(
                        "Nie znaleziono treningu o podanym ID dla tego użytkownika"
                ));
        trainingRepository.delete(training);

    }


    public TrainingStatisticsDTO getStatisticsForUserId(Long userId) {
        List<Training> trainings = trainingRepository.findAllByUserAssigned_Id(userId);

        if (trainings.isEmpty()) {
            return new TrainingStatisticsDTO(0, 0, 0);
        }

        // flatMap bierze wszystkie ćwiczenia ze wszystkich treningów i łączy je w jedną dużą listę, żeby łatwo je podsumować
        // (inaczej spłaszcza strumienie i lączy w jeden duży strumień)
         double averageDuration = trainings.stream()
                .flatMap(t->t.getExercises().stream())
                .mapToDouble(TrainingExercise::getDuration)
                .average()
                .orElse(0.0);
        double averageAmount = trainings.stream()
                .flatMap(t->t.getExercises().stream())
                .mapToDouble(TrainingExercise::getAmount)
                .average()
                .orElse(0.0);
        double averageIntensityScore = trainings.stream()
                .mapToDouble(Training::getIntensityScore)
                .average()
                .orElse(0.0);

        return new TrainingStatisticsDTO(averageDuration, averageAmount, averageIntensityScore);
    }

    public List<TrainingDTO> getTrainingsForUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika po emailu " + email));
        List<Training> trainings = trainingRepository.findAllByUserAssigned_Id(user.getId());

        return trainings.stream()
                .map(this::mapToTrainingDTO)
                .collect(Collectors.toList());
    }

    private TrainingDTO mapToTrainingDTO(Training training) {
        return new TrainingDTO(
                training.getId(),
                training.getUserAssigned().getId(),
                training.getDate(),
                training.getCreatedAt(),
                getExerciseMethod(training)
        );
    }


    public void calculateTiS(Training training) {


        if(training.getExercises() == null || training.getExercises().isEmpty()){
            training.setIntensityScore(0);
            training.setIntensityScoreMessage("Brak ćwiczeń w treningu");
            return;
        }


        int totalDuration = training.getExercises().stream()
                .mapToInt(TrainingExercise::getDuration)
                .sum();
        int totalAmount = training.getExercises().stream()
                .mapToInt(TrainingExercise::getAmount)
                .sum();

        // Wzór: Czas + (powtórzenia * 2)
        // Math.min(..., 100) = maksymalnie 100, Math.max(0, ...) = minimalnie 0
        // Najpierw ograniczam górną granicę, potem dolną, wynik zawsze w przedziale 0–100

        double TiS = Math.max(0, Math.min(totalDuration + (totalAmount * 2), 100));
        String feedback;

        if (TiS < 40) feedback = "Lekki Trening";
        else if (TiS < 70) feedback = "Dobry Trening";
        else feedback = "Bardzo dobry Trening";


        training.setIntensityScore(TiS);
        training.setIntensityScoreMessage(feedback);

    }
}
