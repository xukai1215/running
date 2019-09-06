package com.example.running.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WebsiteResultDTO {

    String id;
    String address;
    String manager;
    String email;
    String interval;
    int status;
}
