package com.systemankiet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

// VIA_DTO: Page<T> serializowany przez stabilne DTO zamiast wewnetrznego PageImpl
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
public class SystemAnkietApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemAnkietApplication.class, args);
    }
}
