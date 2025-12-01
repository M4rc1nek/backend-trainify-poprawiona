package com.trainify.trainifybackend.training.service;

import com.trainify.trainifybackend.exception.TrainingForUserNotFoundException;
import com.trainify.trainifybackend.exception.UserNotFoundException;
import com.trainify.trainifybackend.training.dto.TrainingDTO;
import com.trainify.trainifybackend.training.dto.TrainingExerciseDTO;
import com.trainify.trainifybackend.training.dto.TrainingPlanDTO;
import com.trainify.trainifybackend.training.dto.TrainingStatisticsDTO;
import com.trainify.trainifybackend.training.model.Training;
import com.trainify.trainifybackend.training.model.TrainingExercise;
import com.trainify.trainifybackend.training.model.exerciseModel.*;
import com.trainify.trainifybackend.training.repository.TrainingRepository;
import com.trainify.trainifybackend.user.model.User;
import com.trainify.trainifybackend.user.repository.UserRepository;
import com.trainify.trainifybackend.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingService {


    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final GenerateTisFeedbackService generateTisFeedbackService;

    @Transactional
    public TrainingDTO addTraining(TrainingDTO trainingDTO) {

        User user = userService.getUserById(trainingDTO.userId())
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika z id " + trainingDTO.userId()));
        Training training = Training.builder()
                .date(trainingDTO.date())
                .createdAt(LocalDateTime.now())
                .note(trainingDTO.note())
                .intensityScore(trainingDTO.intensityScore())
                .intensityScoreMessage(trainingDTO.intensityScoreMessage())
                .userAssigned(user)
                .build();


        //Sprawdzam, czy lista ćwiczeń w dto istnieje i sprawdzam, czy lista nie jest pusta, jeżeli się zgadza tworze ćwiczenia
        if (!CollectionUtils.isEmpty(trainingDTO.exercises())) {
            List<TrainingExercise> exercises = createExercise(trainingDTO, training);
            training.setExercises(exercises);
            calculateTiS(training);
        }


        trainingRepository.save(training);
        List<TrainingExerciseDTO> getExercise = getExercise(training);

        return new TrainingDTO(
                training.getId(),
                user.getId(),
                training.getNote(),
                training.getIntensityScore(),
                training.getIntensityScoreMessage(),
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
        existingTraining.setNote(trainingDTO.note()); // aktualizacja notatki

        //Zastępuje stare ćwiczenia nowymi, tak żeby Hibernate poprawnie usuwał te usunięte.
        List<TrainingExercise> newExercises = createExercise(trainingDTO, existingTraining);
        if (existingTraining.getExercises() == null) {
            existingTraining.setExercises(newExercises);
        } else {
            existingTraining.getExercises().clear();
            existingTraining.getExercises().addAll(newExercises);
        }

        calculateTiS(existingTraining);
        trainingRepository.save(existingTraining);

        List<TrainingExerciseDTO> getExercise = getExercise(existingTraining);

        return new TrainingDTO(
                existingTraining.getId(),

                userId,
                existingTraining.getNote(),
                existingTraining.getIntensityScore(),
                existingTraining.getIntensityScoreMessage(),
                existingTraining.getDate(),
                existingTraining.getCreatedAt(),
                getExercise

        );
    }

    public List<TrainingExercise> createExercise(TrainingDTO trainingDTO, Training training) {
        return trainingDTO.exercises().stream()
                .map(dto -> {

                    // Pobieramy nazwę ćwiczenia i jego "przyjazną" nazwę do wyświetlania
                    String name = dto.exerciseName(); // np. "DIPY_NA_PORECZACH" – nazwa w enum
                    String displayName = dto.exerciseDisplayName(); //  np. "Dipy na poręczach" – do pokazania użytkownikowi

                    /* Jeśli displayName jest puste, używamy enumów, aby dopasować techniczną nazwę do czytelnej nazwy
                    valueOf(name) zamienia String w odpowiadającą stałą enumu (np. "POMPKI_KLASYCZNE" → ChestExercise.POMPKI_KLASYCZNE)*/
                    if (displayName == null && name != null) {
                        switch (dto.exerciseCategory()) {
                            case Klata -> displayName = ChestExercise.valueOf(name).getNazwa();
                            // valueOf(name) – znajdź element enum ChestExercise o dokładnie tej nazwie (np. "POMPKI_KLASYCZNE")
                            // getNazwa() – pobierz "czytelną" nazwę ćwiczenia np. "Pompki klasyczne"
                            case Plecy -> displayName = BackExercise.valueOf(name).getNazwa();
                            case Barki -> displayName = ShoulderExercise.valueOf(name).getNazwa();
                            case Ramiona -> displayName = ArmExercise.valueOf(name).getNazwa();
                            case Brzuch -> displayName = AbsExercise.valueOf(name).getNazwa();
                            case Nogi -> displayName = LegExercise.valueOf(name).getNazwa();
                        }
                    }

                    return TrainingExercise.builder()
                            .id(dto.id())
                            .exerciseCategory(dto.exerciseCategory())
                            .exerciseName(name)
                            .exerciseDisplayName(displayName)
                            .amount(dto.amount())
                            .duration(dto.duration())
                            .trainingAssigned(training)
                            .build();
                })
                .collect(Collectors.toList());
    }


    public List<TrainingExerciseDTO> getExercise(Training training) {
        List<TrainingExercise> exercises = training.getExercises();
        if (exercises == null) {
            return Collections.emptyList();
        }
        return exercises.stream().map(
                        exercise -> new TrainingExerciseDTO(
                                exercise.getId(),
                                exercise.getExerciseCategory(),
                                exercise.getExerciseName(),
                                exercise.getExerciseDisplayName(),
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


    public TrainingDTO addReadyPlanToUser(Long userId, TrainingPlanDTO trainingPlanDTO) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika o id " + userId));

        //Tworzymy nowy trening
        Training training = Training.builder()
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .note(trainingPlanDTO.name())
                .userAssigned(user)
                .build();

        // Tworzymy listę ćwiczeń dla treningu na podstawie DTO
        List<TrainingExercise> exercises = trainingPlanDTO.exercises().stream()
                .map(dto -> {
                    String displayName = dto.exerciseDisplayName();
                    String exerciseName = dto.exerciseName();

                    if ((displayName == null || displayName.isEmpty()) && exerciseName != null) {
                        switch (dto.exerciseCategory()) {
                            case Klata -> displayName = ChestExercise.valueOf(exerciseName).getNazwa();
                            case Plecy -> displayName = BackExercise.valueOf(exerciseName).getNazwa();
                            case Barki -> displayName = ShoulderExercise.valueOf(exerciseName).getNazwa();
                            case Ramiona -> displayName = ArmExercise.valueOf(exerciseName).getNazwa();
                            case Brzuch -> displayName = AbsExercise.valueOf(exerciseName).getNazwa();
                            case Nogi -> displayName = LegExercise.valueOf(exerciseName).getNazwa();
                        }
                    }
                    // Tworzymy obiekt TrainingExercise i przypisujemy ćwiczenia z powyżej do treningu utworzonego wcześniej
                    return TrainingExercise.builder()
                            .exerciseCategory(dto.exerciseCategory())
                            .exerciseName(exerciseName)
                            .exerciseDisplayName(displayName)
                            .amount(dto.amount())
                            .duration(dto.duration())
                            .trainingAssigned(training)
                            .build();
                })
                .collect(Collectors.toList());


        training.setExercises(exercises);
        calculateTiS(training);
        trainingRepository.save(training);

        List<TrainingExerciseDTO> getExercise = getExercise(training);

        return new TrainingDTO(
                training.getId(),
                userId,
                training.getNote(),
                training.getIntensityScore(),
                training.getIntensityScoreMessage(),
                training.getDate(),
                training.getCreatedAt(),
                getExercise
        );
    }

    public TrainingStatisticsDTO getStatisticsForUserId(Long userId) {
        List<Training> trainings = trainingRepository.findAllByUserAssigned_Id(userId);

        if (trainings.isEmpty()) {
            return new TrainingStatisticsDTO(0, 0, 0);
        }


        //średnia czasu całego treningu -> sumujemy czas wszystkich ćwiczeń w treningu, potem średnia po treningach
        double averageDuration = trainings.stream()
                .mapToDouble(t -> t.getExercises().stream()
                        .mapToInt(TrainingExercise::getDuration)
                        .sum()
                )
                .average()
                .orElse(0.0);

        //średnia liczby powtórzeń -> sumujemy wszystkie ćwiczenia w treningu, potem średnia po treningach
        double averageAmount = trainings.stream()
                .mapToDouble(t -> t.getExercises().stream()
                        .mapToInt(TrainingExercise::getAmount)
                        .sum()
                )
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
                training.getNote(),
                training.getIntensityScore(),
                training.getIntensityScoreMessage(),
                training.getDate(),
                training.getCreatedAt(),
                getExercise(training)
        );
    }


    public void calculateTiS(Training training) {


        if (training.getExercises() == null || training.getExercises().isEmpty()) {
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

      /*  Math.min(..., 100) = maksymalnie 100, Math.max(0, ...) = minimalnie 0
          Najpierw ograniczam górną granicę, potem dolną, wynik zawsze w przedziale 0–100

         Normalizacja czasu i ilości powtórzeń do zakresu 0–1
         80 minut i 500 powtórzeń to maksymalne wartości przy pełnym wyniku*/
        double calculateDuration = Math.min(totalDuration / 60.0, 1.0);
        double calculateAmount = Math.min(totalAmount / 300.0, 1.0);

        double TiS = (calculateDuration * 0.5 + calculateAmount * 0.5) * 100;
        String feedback;

        if (TiS < 40) feedback = generateTisFeedbackService.generateRandomLightFeedback();  // Dodanie losowych zdan 50 losowych dla kazdego, uzycie random
        else if (TiS < 70) feedback = generateTisFeedbackService.generateRandomGoodFeedback();
        else feedback = generateTisFeedbackService.generateRandomVeryGoodFeedback();


        training.setIntensityScore(TiS);
        training.setIntensityScoreMessage(feedback);

    }

}
