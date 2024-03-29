package com.app.security;

import com.app.security.jwt.JwtAuthorizationFilter;
import com.app.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Value("${api.client.angular}")
    private String clientAngular;
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    /*
     * Se establece una cadena de filtros de seguridad en toda la aplicacion
     * Aquí se determinan los permisos según los roles de usuario para acceder a la
     * aplicación y demas configuraciones
     */
    @Bean
    SecurityFilterChain securityFilterChain (HttpSecurity http, AuthenticationManager authenticationManager)
    throws Exception{

        return http
                // Se deshabilita Cross-site request forgery
                .csrf(config->config.disable())
                // Permite la gestión de sesiones de tipo STATELESS osea que son sin estado
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //Configuración de acceso a los endpoints
                .authorizeHttpRequests(
                        auth-> {
                            //Endpoint de documentación
                            auth.requestMatchers
                                    ("/swagger-ui.html","/v3/api-docs/**","/swagger-ui/**").permitAll();
                            auth.requestMatchers("/api/v1/auth/login","/api/v1/auth/register").permitAll();
                            auth.requestMatchers("/api/v1/subs/**").authenticated();
                            auth.requestMatchers("/api/v1/subs/types/**").authenticated();
                            auth.requestMatchers("/api/v1/stripe/**").authenticated();
                            auth.requestMatchers("/api/v1/users/**").permitAll();
                            auth.anyRequest().authenticated();
                        })
                //Filtro creado el cual es necesario para autorizar utilizando un token
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    //Objeto el cual se encarga de la encriptación de contraseñas
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter(){
        return new JwtAuthorizationFilter();
    }

    //Este objeto se encarga de la administración de la autenticación de los usuarios
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity httpSecurity,PasswordEncoder passwordEncoder)
            throws Exception {
        //Creación de AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder =
                httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        //Se establece el propiedades al objeto creado
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();

    }
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/api/v1/**")
                        .allowedMethods("*")
                        .allowedOrigins(clientAngular)
                        .exposedHeaders("*");
            }

        };

    }


    }
