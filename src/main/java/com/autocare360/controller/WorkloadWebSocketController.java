package com.autocare360.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WorkloadWebSocketController {

    @MessageMapping("/workload/refresh")
    @SendTo("/topic/workload-update")
    public String refreshWorkload(String message) {
        return "Workload data updated: " + message;
    }

    @MessageMapping("/schedule/refresh")
    @SendTo("/topic/schedule-update")
    public String refreshSchedule(String message) {
        return "Schedule updated for employee: " + message;
    }
}