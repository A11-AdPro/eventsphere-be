package id.ac.ui.cs.advprog.eventsphere.authentication.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    // Authentication metrics
    @Bean
    public Counter loginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_login_success_total")
                .description("Total number of successful login attempts")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_login_failure_total")
                .description("Total number of failed login attempts")
                .register(meterRegistry);
    }

    @Bean
    public Counter registrationSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_registration_success_total")
                .description("Total number of successful user registrations")
                .register(meterRegistry);
    }

    @Bean
    public Counter registrationFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_registration_failure_total")
                .description("Total number of failed user registration attempts")
                .register(meterRegistry);
    }

    // Admin operations metrics
    @Bean
    public Counter adminUserCreationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("admin_user_creation_total")
                .description("Total number of users created by admin")
                .register(meterRegistry);
    }

    @Bean
    public Counter adminUserUpdateCounter(MeterRegistry meterRegistry) {
        return Counter.builder("admin_user_update_total")
                .description("Total number of user updates performed by admin")
                .register(meterRegistry);
    }

    @Bean
    public Counter adminUserDeletionCounter(MeterRegistry meterRegistry) {
        return Counter.builder("admin_user_deletion_total")
                .description("Total number of user deletions performed by admin")
                .register(meterRegistry);
    }

    @Bean
    public Counter adminUserRetrievalCounter(MeterRegistry meterRegistry) {
        return Counter.builder("admin_user_retrieval_total")
                .description("Total number of user data retrievals by admin")
                .register(meterRegistry);
    }

    // API endpoint metrics
    @Bean
    public Counter getCurrentUserCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_get_current_user_total")
                .description("Total number of get current user requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter jwtTokenGenerationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_jwt_token_generation_total")
                .description("Total number of JWT tokens generated")
                .register(meterRegistry);
    }

    // Error metrics
    @Bean
    public Counter authenticationErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_error_total")
                .description("Total number of authentication errors")
                .tag("type", "general")
                .register(meterRegistry);
    }

    @Bean
    public Counter emailAlreadyExistsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_email_already_exists_total")
                .description("Total number of registration attempts with existing email")
                .register(meterRegistry);
    }

    @Bean
    public Counter userNotFoundCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_user_not_found_total")
                .description("Total number of user not found errors")
                .register(meterRegistry);
    }

    // Role-based metrics
    @Bean
    public Counter attendeeRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_attendee_registration_total")
                .description("Total number of attendee registrations")
                .register(meterRegistry);
    }

    @Bean
    public Counter organizerRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_organizer_registration_total")
                .description("Total number of organizer registrations")
                .register(meterRegistry);
    }

    @Bean
    public Counter adminRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_admin_registration_total")
                .description("Total number of admin registrations")
                .register(meterRegistry);
    }

    // Security metrics
    @Bean
    public Counter unauthorizedAccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_unauthorized_access_total")
                .description("Total number of unauthorized access attempts")
                .register(meterRegistry);
    }

    @Bean
    public Counter forbiddenAccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth_forbidden_access_total")
                .description("Total number of forbidden access attempts")
                .register(meterRegistry);
    }
}