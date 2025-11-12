package com.trainify.trainifybackend.training.model;


import com.trainify.trainifybackend.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Training {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date; // data treningu
    private LocalDateTime createdAt;// kiedy wpis został dodany


    private double intensityScore; // Wynik intensywnosci treningu 0 - 100
    private String intensityScoreMessage; // Komunikat jak intensywny trening był


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userAssigned;


    @OneToMany(mappedBy = "trainingAssigned", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingExercise> exercises;


}
