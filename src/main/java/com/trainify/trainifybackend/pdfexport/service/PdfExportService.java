package com.trainify.trainifybackend.pdfexport.service;

import com.trainify.trainifybackend.training.dto.TrainingDTO;
import com.trainify.trainifybackend.training.dto.TrainingExerciseDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] generateTrainingsPdf(List<TrainingDTO> trainings) { // byte dlatego ze pliki PDF operuja na byte


        if (trainings.isEmpty()) {
            throw new RuntimeException("Brak treningów");
        }

        try (PDDocument document = new PDDocument();  // tworzysz dokument PDF
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {


            PDType0Font fontBold = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/arialbd.ttf"));
            PDType0Font fontRegular = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/arial.ttf"));

            PDPage page = new PDPage(); // nowa strona PDF
            document.addPage(page); //do pdfa dodajesz strone

            PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            int y = 750; // Wysokosc startowa na stronie (od gory)
            int lineHeight = 20; //  Odległość między kolejnymi liniami tekstu


            for (TrainingDTO training : trainings) {

                if(y < 50){
                    content.close();
                    page = new PDPage();
                    document.addPage(page);
                    content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                    y = 750;
                }

                content.beginText(); // Rozpoczynamy pisanie tekstu
                content.setFont(fontBold, 14); // Ustawiamy pogrubioną czcionkę o rozmiarze 14
                content.newLineAtOffset(50, y); // Ustawiamy pozycję startową tekstu (x=50, y)
                content.showText("Trening: " + training.date()); // Wypisujemy tekst z datą treningu
                content.endText(); // Kończymy pisanie tekstu
                y -= lineHeight; // Przesuwamy kursor w dół o wysokość jednej linii

                if(y < 50){
                    content.close();
                    page = new PDPage();
                    document.addPage(page);
                    content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                    y = 750;
                }
                content.beginText();
                content.setFont(fontRegular, 12);
                content.newLineAtOffset(50, y);
                content.showText("Notatka: " + training.note() + " | Intensywność: " + training.intensityScore());
                content.endText();
                y -= lineHeight;


                // Ćwiczenia
                for (TrainingExerciseDTO ex : training.exercises()) {
                    if(y < 50){
                        content.close();
                        page = new PDPage();
                        document.addPage(page);
                        content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                        y = 750;
                    }
                    content.beginText();
                    content.setFont(fontRegular, 12);
                    content.newLineAtOffset(60, y);
                    content.showText("- " + ex.exerciseDisplayName() + ": " + ex.amount() + " powt., " + ex.duration() + " min");
                    content.endText();
                    y -= lineHeight;
                }
                y -= 10;
            }


            content.beginText();
            content.setFont(fontBold, 14);
            content.newLineAtOffset(50, y);
            content.showText("Dziękujemy za korzystanie z Trainify");
            content.endText();
            y -= lineHeight;


            content.beginText();
            content.setFont(fontRegular, 18);
            content.newLineAtOffset(50,y);
            content.showText("( ͡° ͜ʖ ͡°)");
            content.endText();
            y -= lineHeight;

            content.close();

            document.save(byteArrayOutputStream);  //zapisujesz dokument
            return byteArrayOutputStream.toByteArray(); // zwracasz PDF jako tablicę bajtów

        } catch (Exception e) {
            throw new RuntimeException("Błąd przy generowaniu PDF ", e);
        }

    }
}
