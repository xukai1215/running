package com.example.running.dao;

import com.example.running.entity.Website;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebsiteDao extends MongoRepository<Website,String> {

    Website findFirstById(String id);

    Website findFirstByAddress(String address);

}
