package id.ac.ui.cs.advprog.eventsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventSphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventSphereApplication.class, args);
    }
}