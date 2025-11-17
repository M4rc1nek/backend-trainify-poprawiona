package com.trainify.trainifybackend.dailywellness.repository;

import com.trainify.trainifybackend.dailywellness.model.DailyWellness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyWellnessRepository extends JpaRepository<DailyWellness, Long> {


    @Query("SELECT d FROM DailyWellness d WHERE d.userAssigned.id = :userId AND d.date = :date")
    Optional<DailyWellness> findByUserAssigned_IdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);



    // Pobiera wszystkie wpisy DailyWellness dla konkretnego użytkownika (userId)
    // i zwraca je posortowane od najnowszego do najstarszego według daty.
    @Query("SELECT d FROM DailyWellness d WHERE d.userAssigned.id = :userId ORDER BY d.date DESC")
    List<DailyWellness> findAllByUserAssigned_IdOrderByDateDesc(@Param("userId") Long userId);


}
