package com.dira.mqbridge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dira.mqbridge.dto.MqSendRequest;
import com.dira.mqbridge.dto.MqSendResponse;
import com.dira.mqbridge.service.MqClientService;

@RestController
@RequestMapping("/api/v1")
public class MqBridgeController {

    private final MqClientService mqClientService;

    public MqBridgeController(MqClientService mqClientService) {
        this.mqClientService = mqClientService;
    }

    @PostMapping("/send")
    public ResponseEntity<MqSendResponse> send(@RequestBody MqSendRequest request) {
        if (request.getCredentials() == null) {
            return ResponseEntity.badRequest().body(MqSendResponse.fail("credentials is required"));
        }
        if (request.getInputQueue() == null || request.getMessage() == null) {
            return ResponseEntity.badRequest().body(MqSendResponse.fail("input_queue and message are required"));
        }
        if (request.getOutputQueue() == null) {
            return ResponseEntity.badRequest().body(MqSendResponse.fail("output_queue is required"));
        }

        MqSendResponse response = mqClientService.sendAndReceive(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.internalServerError().body(response);
    }
}
