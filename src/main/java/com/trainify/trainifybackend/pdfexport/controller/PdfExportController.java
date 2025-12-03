package com.trainify.trainifybackend.pdfexport.controller;

import com.trainify.trainifybackend.pdfexport.service.PdfExportService;
import com.trainify.trainifybackend.training.dto.TrainingDTO;
import com.trainify.trainifybackend.training.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfExportController {

    private final PdfExportService pdfExportService;
    private final TrainingService trainingService;

    @GetMapping("/trainingsPdf")
    public ResponseEntity<byte[]> getTrainingsPdf(@RequestParam String email) {

        //Wyciągasz treningi dla użytkownika po emailu
        List<TrainingDTO> trainings = trainingService.getTrainingsForUserByEmail(email);

        byte[] pdfBytes = pdfExportService.generateTrainingsPdf(trainings); // wywolanie serwisu ktory tworzy PDF

        //Dzięki temu przeglądarka wie, że odpowiedź to plik PDF do pobrania z nazwą weekly_trainings.pdf
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        httpHeaders.setContentDispositionFormData("attachment", "trainings_history.pdf");

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(pdfBytes);
    }
}
