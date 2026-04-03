package com.labservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * THE ENTRY POINT — where your entire application starts.
 *
 * What happens when this runs:
 * 1. Spring Boot starts an embedded Tomcat web server (port 8080)
 * 2. It scans ALL sub-packages (model, controller, service, etc.)
 *    looking for annotations like @Entity, @Service, @RestController
 * 3. It auto-configures everything: database connection, security, JSON handling
 * 4. Your app is now ready to receive HTTP requests!
 *
 * @SpringBootApplication is actually THREE annotations in one:
 *   - @Configuration     → "This class can define beans (reusable objects)"
 *   - @EnableAutoConfiguration → "Spring, configure everything automatically"
 *   - @ComponentScan     → "Scan all sub-packages for annotated classes"
 */
@SpringBootApplication
public class LabServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabServiceApplication.class, args);
        // That's it! One line starts the entire web server.
        // Spring Boot handles everything else automatically.
    }
}
