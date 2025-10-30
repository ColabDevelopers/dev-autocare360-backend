package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean serviceUpdates;
    private Boolean appointmentReminders;
}

