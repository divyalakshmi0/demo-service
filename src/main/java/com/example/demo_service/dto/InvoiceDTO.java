package com.example.demo_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private String name;
    private String weekEnd;
    private Double stRate;
    private Double stUnits;
    private Double otRate;
    private Double otUnits;
    private Double amount;
}
