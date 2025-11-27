package com.trainify.trainifybackend.contact.controller;

import com.trainify.trainifybackend.contact.dto.ContactDTO;
import com.trainify.trainifybackend.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;


    @PostMapping("/contact")
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody ContactDTO contactDTO){
        contactService.sendContactEmail(contactDTO);
        return ResponseEntity.ok().build();
    }

}
