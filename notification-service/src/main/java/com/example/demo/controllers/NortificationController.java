package com.example.demo.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.NortificationPayLoad;

@RestController
@RequestMapping("/api/notifications")
public class NortificationController {

    private static final Logger log = LoggerFactory.getLogger(NortificationController.class);

    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody NortificationPayLoad payload) {
        log.info("NOTIFICATION: txn={} message='{}' to={}",
                payload.getTransactionId(),
                payload.getMessage(),
                payload.getTo());

        return ResponseEntity.accepted().build();
    }
}
