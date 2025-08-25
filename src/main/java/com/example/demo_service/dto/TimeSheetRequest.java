package com.example.demo_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSheetRequest {
    private String employeeId;
    private String name;
    private String date;
    private String clockIn;
    private String clockOut;
    private double hoursWorked;
    private String project;
    private String taskDescription;
}
