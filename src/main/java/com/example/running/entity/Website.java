package com.example.running.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
@Data
public class Website {
    @Id
    String id;
    String address;
    String manager;
    String email;
    String interval;
    int status;
}
