package com.trainify.trainifybackend.dailywellness.service;


import com.trainify.trainifybackend.dailywellness.dto.DailyWellnessDTO;
import com.trainify.trainifybackend.dailywellness.model.DailyWellness;
import com.trainify.trainifybackend.dailywellness.model.ReadinessLevel;
import com.trainify.trainifybackend.dailywellness.repository.DailyWellnessRepository;
import com.trainify.trainifybackend.exception.DailyWellnessAlreadySubmittedException;
import com.trainify.trainifybackend.exception.DailyWellnessForUserNotFoundException;
import com.trainify.trainifybackend.exception.UserNotFoundException;
import com.trainify.trainifybackend.user.model.User;
import com.trainify.trainifybackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyWellnessService {

    private final DailyWellnessRepository dailyWellnessRepository;
    private final UserRepository userRepository;

    @Transactional
    public DailyWellnessDTO submitCheck(DailyWellnessDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika"));


        dailyWellnessRepository.findByUserAssigned_IdAndDate(user.getId(), LocalDate.now())
                .ifPresent(w -> {
                    throw new DailyWellnessAlreadySubmittedException("Dzisiaj formularz został już wypełniony");
                });


        int readinessScore = calculateReadiness(dto);
        ReadinessLevel readinessLevel = determineReadinessLevel(readinessScore);
        String recommendation = generateRecommendation(readinessLevel);


        DailyWellness dailyWellness = DailyWellness.builder()
                .date(LocalDate.now()) //LocalDate.now() zamiast dto.date() -> dlatego aby data była pobierana z teraz, żeby użytkownik nie mógł wpisać np. date jutrzejszą
                .hoursSlept(dto.hoursSlept())
                .energyLevel(dto.energyLevel())
                .musclePain(dto.musclePain())
                .mood(dto.mood())
                .motivation(dto.motivation())
                .readinessScore(readinessScore)
                .readinessLevel(readinessLevel)
                .recommendation(recommendation)
                .userAssigned(user)
                .build();

        dailyWellnessRepository.save(dailyWellness);

        return new DailyWellnessDTO(
                dailyWellness.getId(),
                dailyWellness.getDate(),
                dailyWellness.getHoursSlept(),
                dailyWellness.getEnergyLevel(),
                dailyWellness.getMusclePain(),
                dailyWellness.getMood(),
                dailyWellness.getMotivation(),
                dailyWellness.getReadinessScore(),
                dailyWellness.getReadinessLevel(),
                dailyWellness.getRecommendation()
        );
    }

    public List<DailyWellnessDTO> getDailyWellnessHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika"));
        List<DailyWellness> history = dailyWellnessRepository.findAllByUserAssigned_IdOrderByDateDesc(user.getId());

        return history.stream()
                .map(d -> new DailyWellnessDTO(
                        d.getId(),
                        d.getDate(),
                        d.getHoursSlept(),
                        d.getEnergyLevel(),
                        d.getMusclePain(),
                        d.getMood(),
                        d.getMotivation(),
                        d.getReadinessScore(),
                        d.getReadinessLevel(),
                        d.getRecommendation()
                ))
                .toList();
    }

    @Transactional
    public DailyWellnessDTO updateDailyWellness(DailyWellnessDTO dto, Long userId, LocalDate date) {
       DailyWellness existing = dailyWellnessRepository.findByUserAssigned_IdAndDate(userId, date)
                .orElseThrow(() -> new DailyWellnessForUserNotFoundException("Nie znaleziono DailyWellness dla użytkownika o ID: " + userId + " w dniu " + date));

        int readinessScore = calculateReadiness(dto);
        ReadinessLevel readinessLevel = determineReadinessLevel(readinessScore);
        String recommendation = generateRecommendation(readinessLevel);


        DailyWellness updatedDailyWellness = DailyWellness.builder()
                .id(existing.getId())
                .userAssigned(existing.getUserAssigned())
                .date(dto.date())
                .hoursSlept(dto.hoursSlept())
                .energyLevel(dto.energyLevel())
                .musclePain(dto.musclePain())
                .mood(dto.mood())
                .motivation(dto.motivation())
                .readinessScore(readinessScore)
                .readinessLevel(readinessLevel)
                .recommendation(recommendation)
                .build();


        DailyWellness saved =  dailyWellnessRepository.save(updatedDailyWellness);


        return new DailyWellnessDTO(
                saved.getId(),
                saved.getDate(),
                saved.getHoursSlept(),
                saved.getEnergyLevel(),
                saved.getMusclePain(),
                saved.getMood(),
                saved.getMotivation(),
                saved.getReadinessScore(),
                saved.getReadinessLevel(),
                saved.getRecommendation()
        );
    }


    public void deleteDailyWellness(Long userId, LocalDate date) {
        DailyWellness dailyWellness = dailyWellnessRepository.findByUserAssigned_IdAndDate(userId, date)
                .orElseThrow(() -> new DailyWellnessForUserNotFoundException("Nie znaleziono DailyWellness dla użytkownika o ID: " + userId + " w dniu " + date));
        dailyWellnessRepository.delete(dailyWellness);
    }


    private int calculateReadiness(DailyWellnessDTO dto) {

        double sleepPoints;

        if (dto.hoursSlept() >= 8) sleepPoints = 30;
        else if (dto.hoursSlept() >= 7) sleepPoints = 25;
        else if (dto.hoursSlept() >= 6) sleepPoints = 15;
        else sleepPoints = 5;

        double energyPoints = (dto.energyLevel() / 10.0) * 25;
        double musclePainPoints = ((10 - dto.musclePain()) / 10.0) * 25;
        double moodPoints = (dto.mood() / 10.0) * 15;
        double motivationPoints = (dto.motivation() / 10.0) * 10;


        return (int) Math.round(sleepPoints + energyPoints + musclePainPoints + moodPoints + motivationPoints);

    }

    private ReadinessLevel determineReadinessLevel(int score) {
        if (score >= 70) return ReadinessLevel.Wysoki;
        else if (score >= 49) return ReadinessLevel.Średni;
        else return ReadinessLevel.Niski;
    }

    private String generateRecommendation(ReadinessLevel readinessLevel) {
        return switch (readinessLevel) {
            case Wysoki -> "Możesz trenować ciężko";
            case Średni -> "Trenuj ostrożnie, słuchaj ciała";
            default -> "Ciało potrzebuje regeneracji, nie trenuj";
        };

    }

}
