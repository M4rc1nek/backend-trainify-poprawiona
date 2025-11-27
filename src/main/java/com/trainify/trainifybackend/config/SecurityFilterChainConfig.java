package com.trainify.trainifybackend.config;

import com.trainify.trainifybackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;


    //Polaczenie z frontendem
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //Włączenie CORS i wyłączenie CSRF
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login").permitAll() //register, /login → dostęp dla wszystkich (nie trzeba tokena JWT).
                        .requestMatchers("/training/**").authenticated() //training/** → dostęp tylko dla zalogowanych użytkowników (czyli muszą mieć ważny token JWT).
                        .requestMatchers("/contact").permitAll()
                        .anyRequest().authenticated()    //anyRequest().authenticated() → każde inne żądanie też wymaga zalogowania.
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // „Nie twórz sesji, nie zapamiętuj zalogowanego użytkownika — wszystko opiera się na tokenie JWT"
                .authenticationProvider(authenticationProvider) // Mowi Springowi jak ma sprawdzac dane
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Wstaw mój filtr JWT przed tym standardowym, żeby token był sprawdzony, zanim Spring zacznie swoje logowanie

        return http.build();
    }
}