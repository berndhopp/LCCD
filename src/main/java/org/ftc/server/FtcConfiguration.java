package org.ftc.server;

import org.gavaghan.geodesy.GeodeticCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
public class FtcConfiguration {
    @Bean
    public GeodeticCalculator geodeticCalculator(){
        return new GeodeticCalculator();
    }
}
