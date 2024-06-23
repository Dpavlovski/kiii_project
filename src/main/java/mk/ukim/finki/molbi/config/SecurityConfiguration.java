package mk.ukim.finki.molbi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Profile(("!cas"))
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends AuthConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        this.authorize(http);
        http.httpBasic((basic) -> basic.realmName("wp.finki.ukim.mk"));
        return http.build();
    }

}
