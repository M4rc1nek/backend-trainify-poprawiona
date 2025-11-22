package com.trainify.trainifybackend.user.model;

import com.trainify.trainifybackend.dailywellness.model.DailyWellness;
import com.trainify.trainifybackend.healthcore.model.UserHealthMetrics;
import com.trainify.trainifybackend.training.model.Training;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.config.annotation.web.oauth2.client.OAuth2ClientSecurityMarker;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "userAssigned", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings;

    @OneToMany(mappedBy = "userAssigned")
    private List<DailyWellness> dailyWellnessHistory;


    @OneToMany(mappedBy = "userAssigned")
    private List<UserHealthMetrics> userHealthMetrics;
}
