package com.trainify.trainifybackend.healthcore.repository;

import com.trainify.trainifybackend.healthcore.model.UserHealthMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthCoreRepository extends JpaRepository<UserHealthMetrics, Long> {

    Optional<UserHealthMetrics> findTopByUserAssigned_IdOrderByIdDesc(Long userId);
}
