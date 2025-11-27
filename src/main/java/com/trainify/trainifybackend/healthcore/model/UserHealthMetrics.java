package com.trainify.trainifybackend.healthcore.model;

import com.trainify.trainifybackend.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Getter
@Setter
public class UserHealthMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int age;

    private double height; // metry
    private double weight;

    private  GenderType genderType;

    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    private double BMI;
    private double BMR;
    private double TDEE;

    private BmiFeedback bmiFeedback;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userAssigned;


}
