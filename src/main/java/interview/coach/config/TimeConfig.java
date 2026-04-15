package interview.coach.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    @Bean
    Clock systemUtcClock() {
        return Clock.systemUTC();
    }
}
