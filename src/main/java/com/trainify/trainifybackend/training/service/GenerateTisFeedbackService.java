package com.trainify.trainifybackend.training.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

@Service
public class GenerateTisFeedbackService {
    private final List<String> lightFeedback = load("tisfeedback/lightFeedback.txt");
    private final List<String> goodFeedback = load("tisfeedback/goodFeedback.txt");
    private final List<String> veryGoodFeedback = load("tisfeedback/veryGoodFeedback.txt");


    private final Random random = new Random();


    public List<String> load(String path) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) throw new IllegalStateException("Nie znaleziono pliku" + path);

            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .filter(line -> !line.isBlank()) // pomija puste linie
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas ładowania pliku");
        }
    }

    public String generateRandomLightFeedback(){
        return  lightFeedback.get(random.nextInt(lightFeedback.size()));
    }

    public String generateRandomGoodFeedback(){
        return  goodFeedback.get(random.nextInt(goodFeedback.size()));
    }

    public String generateRandomVeryGoodFeedback(){
        return  veryGoodFeedback.get(random.nextInt(veryGoodFeedback.size()));
    }


}




