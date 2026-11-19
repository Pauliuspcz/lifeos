package no.kristiania.reportservice.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class EtagConfig {

    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> activityEtagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> filter =
                new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        filter.addUrlPatterns("/api/reports/activity");
        return filter;
    }
}
