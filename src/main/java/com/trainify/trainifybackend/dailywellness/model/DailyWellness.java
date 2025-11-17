package com.trainify.trainifybackend.dailywellness.model;


import com.trainify.trainifybackend.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DailyWellness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private double hoursSlept;
    private int energyLevel;
    private int musclePain;
    private int mood;
    private int motivation;

    private int readinessScore; // Wynik oceny gotowości użytkownika w skali 0-100, wyliczany z 5 parametrów.
    private ReadinessLevel readinessLevel; // Poziom gotowości: "WYSOKI", "ŚREDNI" lub "NISKI".
    private String recommendation; // Krótka rekomendacja treningowa na podstawie wyniku.




    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userAssigned;

}
