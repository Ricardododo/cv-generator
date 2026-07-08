package com.ricardododo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // solo para desarrollo, luego habilita
                .authorizeHttpRequests(auth -> auth
                        //Rutas públicas (sin autenticación)
                        .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        //cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") //Página personalizada de login
                        .loginProcessingUrl("/login") //URL donde se envía el formulario (login por defecto, pero explícito)
                        .defaultSuccessUrl("/dashboard", true) //redirige al dashboard tras el login exitoso
                        .failureUrl("/login?error=true") //si falla, redirige a login sin autenticar
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") //URL para cerrar sesión
                        .logoutSuccessUrl("/login?logout=true") //redirige al login con mensaje de logout
                        .invalidateHttpSession(true) //invalida la sesión
                        .deleteCookies("JSESSIONID") //Elimina la cookie de sesión
                        .permitAll()
                );
        return http.build();
    }
}
