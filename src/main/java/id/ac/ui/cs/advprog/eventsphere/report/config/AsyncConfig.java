package id.ac.ui.cs.advprog.eventsphere.report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // Thread pool untuk notification (operasi ringan, sering terjadi)
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Minimum 3 thread selalu standby
        executor.setCorePoolSize(3);

        // Maksimum 8 thread jika beban tinggi
        executor.setMaxPoolSize(8);

        // Antrian maksimal 50 task
        executor.setQueueCapacity(50);

        executor.setThreadNamePrefix("notification-async-");
        executor.setKeepAliveSeconds(60);

        executor.initialize();
        return executor;
    }

    // Thread pool untuk report processing (operasi berat, jarang terjadi)
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Minimum 2 thread untuk operasi berat
        executor.setCorePoolSize(2);

        // Maksimum 5 thread untuk operasi berat
        executor.setMaxPoolSize(5);

        // Antrian lebih kecil untuk operasi berat
        executor.setQueueCapacity(25);

        executor.setThreadNamePrefix("report-processing-");
        executor.setKeepAliveSeconds(120);

        executor.initialize();
        return executor;
    }
}