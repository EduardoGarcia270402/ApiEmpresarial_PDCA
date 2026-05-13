package com.empresa.tasks.sqa.engine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqaConfig {

    @Bean
    public McCallReportParser mcCallReportParser() {
        return new McCallReportParser();
    }
}