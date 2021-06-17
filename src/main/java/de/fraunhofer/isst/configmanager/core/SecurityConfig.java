package de.fraunhofer.isst.configmanager.core;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * The configuration class helps to allow all requests without login in spring security
 * configuration.
 */
@Configuration
@NoArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * @param http the HttpSecurity object
     * @throws Exception HttpSecurity could not be configured
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().antMatchers("/**").permitAll();
        http.headers().frameOptions().sameOrigin();
    }
}
