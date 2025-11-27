package com.trainify.trainifybackend.healthcore.service;

import com.trainify.trainifybackend.exception.InvalidEnumValueException;
import com.trainify.trainifybackend.exception.MissingRequirementException;
import com.trainify.trainifybackend.exception.UserHealthMetricsNotFoundException;
import com.trainify.trainifybackend.exception.UserNotFoundException;
import com.trainify.trainifybackend.healthcore.dto.UserHealthMetricsDTO;
import com.trainify.trainifybackend.healthcore.model.ActivityLevel;
import com.trainify.trainifybackend.healthcore.model.BmiFeedback;
import com.trainify.trainifybackend.healthcore.model.GenderType;
import com.trainify.trainifybackend.healthcore.model.UserHealthMetrics;
import com.trainify.trainifybackend.healthcore.repository.HealthCoreRepository;
import com.trainify.trainifybackend.user.model.User;
import com.trainify.trainifybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthCalculatorService {

    private final UserRepository userRepository;
    private final HealthCoreRepository healthCoreRepository;


    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika o id " + userId));
    }

    public UserHealthMetrics buildMetrics(Long userId) {
        //Szukasz ostatnich metryk użytkownika (BMI, BMR, TDEE), jeśli są to je zwracasz, jeśli nie ma idzie przechodzisz do .orElseGet
        return healthCoreRepository.findTopByUserAssigned_IdOrderByIdDesc(userId)
                .orElseGet(() -> { //Tworzysz ogólny szkielet metryk, który przypisujesz do użytkownika
                    User user = getUserOrThrow(userId);
                    return UserHealthMetrics.builder()
                            .userAssigned(user)
                            .build();
                }); // Dzięki szkieletowi w innych metodach wystarczy tylko ustawić brakujące wartości, zamiast zawsze tworzyć nowy obiekt.

        //Zamiast zawsze tworzyć nowe metryki, orElseGet zostanie wywołane tylko wtedy, gdy nie znajdziemy istniejących metryk.


    }

    public UserHealthMetrics buildBMI(UserHealthMetricsDTO dto, Long userId) {
        UserHealthMetrics userHealthMetrics = buildMetrics(userId);

        userHealthMetrics.setHeight(dto.height());
        userHealthMetrics.setWeight(dto.weight());
        calculateBMI(userHealthMetrics);

        healthCoreRepository.save(userHealthMetrics);

        return userHealthMetrics;
    }

    public UserHealthMetrics buildBMR(UserHealthMetricsDTO dto, Long userId) {
        UserHealthMetrics userHealthMetrics = buildMetrics(userId);

        userHealthMetrics.setHeight(dto.height());
        userHealthMetrics.setWeight(dto.weight());
        userHealthMetrics.setAge(dto.age());
        userHealthMetrics.setGenderType(dto.genderType());

        calculateBMR(userHealthMetrics);
        healthCoreRepository.save(userHealthMetrics);

        return userHealthMetrics;
    }

    public UserHealthMetrics buildTDEE(UserHealthMetricsDTO dto, Long userId) {
        UserHealthMetrics userHealthMetrics = buildMetrics(userId);

        if (userHealthMetrics.getBMR() == 0.0) {
            throw new MissingRequirementException("Przed obliczeniem TDEE musisz najpierw obliczyć BMR");
        }
        userHealthMetrics.setActivityLevel(dto.activityLevel());

        if (userHealthMetrics.getActivityLevel() == null) {
            throw new MissingRequirementException("ActivityLevel nie może być puste przy liczeniu TDEE");
        }
        calculateTDEE(userHealthMetrics);
        healthCoreRepository.save(userHealthMetrics);

        return userHealthMetrics;
    }


    public UserHealthMetricsDTO mapToDTO(UserHealthMetrics metrics) {
        return new UserHealthMetricsDTO(
                metrics.getId(),
                metrics.getActivityLevel(),
                metrics.getGenderType(),
                metrics.getAge(),
                metrics.getHeight(),
                metrics.getWeight(),
                metrics.getBMI(),
                metrics.getBMR(),
                metrics.getTDEE(),
                metrics.getBmiFeedback()
        );
    }


    public UserHealthMetrics getMetricsOrThrow(Long userId) {
        return healthCoreRepository.findTopByUserAssigned_IdOrderByIdDesc(userId)
                .orElseThrow(() -> new UserHealthMetricsNotFoundException("Brak zapisanych metryk zdrowotnych dla użytkownika o id: " + userId));
    }

    public double getBMI(Long userId) {
        return getMetricsOrThrow(userId).getBMI();
    }

    public double getBMR(Long userId) {
        return getMetricsOrThrow(userId).getBMR();
    }

    public double getTDEE(Long userId) {
        return getMetricsOrThrow(userId).getTDEE();
    }


    private void calculateBMI(UserHealthMetrics userHealthMetrics) {
        double heightInMeters = userHealthMetrics.getHeight() / 100.0;
        double BMI = userHealthMetrics.getWeight() / (heightInMeters * heightInMeters);

        BmiFeedback feedback;

        if (BMI < 18.5) feedback = BmiFeedback.niedowaga;
        else if (BMI <= 24.9) feedback = BmiFeedback.prawidłowa;
        else if (BMI <= 29.9) feedback = BmiFeedback.nadwaga;
        else feedback = BmiFeedback.otyłość;


        userHealthMetrics.setBMI(BMI);
        userHealthMetrics.setBmiFeedback(feedback);
    }


    private void calculateBMR(UserHealthMetrics userHealthMetrics) {

        GenderType genderType = userHealthMetrics.getGenderType();
        double BMR;

        switch (genderType) {
            case Mężczyzna ->
                    BMR = 10 * userHealthMetrics.getWeight() + 6.25 * userHealthMetrics.getHeight() - 5 * userHealthMetrics.getAge() + 5;
            case Kobieta ->
                    BMR = 10 * userHealthMetrics.getWeight() + 6.25 * userHealthMetrics.getHeight() - 5 * userHealthMetrics.getAge() - 161;
            default -> throw new InvalidEnumValueException("Nie ma takiej płci:  " + genderType);
        }

        userHealthMetrics.setBMR(BMR);

    }

    private void calculateTDEE(UserHealthMetrics userHealthMetrics) {

        ActivityLevel activityLevel = userHealthMetrics.getActivityLevel();
        double PAL; // to samo co activityLevel ale wartości nie enum (Physical Activity Level)

        switch (activityLevel) {
            case Brak -> PAL = 1.2;
            case Niski -> PAL = 1.375;
            case Umiarkowany -> PAL = 1.55;
            case Wysoki -> PAL = 1.7;
            case Ekstremalny -> PAL = 1.9;
            default -> throw new InvalidEnumValueException("Nie ma takiej aktywności:  " + activityLevel);
        }

        double TDEE = userHealthMetrics.getBMR() * PAL;

        userHealthMetrics.setTDEE(TDEE);


    }


}
