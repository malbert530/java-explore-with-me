package ru.practicum;

import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Setter
@Configuration
public class StatsClientConfig {

    @Bean
    StatsClient statsClient() {
        return new StatsClient(new RestTemplate());
    }
}
