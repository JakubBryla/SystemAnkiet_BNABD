package com.systemankiet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Punkt wejścia aplikacji Spring Boot.
 * VIA_DTO: Page<T> serializowany przez stabilne DTO zamiast wewnętrznego PageImpl —
 * zapewnia spójną strukturę JSON paginacji niezależnie od wersji Spring Data.
 */
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
public class SystemAnkietApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemAnkietApplication.class, args);
    }
}
