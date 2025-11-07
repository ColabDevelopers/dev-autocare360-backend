package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleDto {
    private String date;
    private String dayOfWeek;
    private List<ScheduledTaskDto> tasks;
    private Double totalHours;
}
