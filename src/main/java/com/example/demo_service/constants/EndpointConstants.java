package com.example.demo_service.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointConstants {

    public static final String BASE_URL = "/api";
    public static final String DOWNLOAD_INVOICE = "/v1.0/invoice";
    public static final String CREATE_TIMESHEET = "/v1.0/time-sheet";
    public static final String DOWNLOAD_TIMESHEET = "/v1.0/download/time-sheet";

}

