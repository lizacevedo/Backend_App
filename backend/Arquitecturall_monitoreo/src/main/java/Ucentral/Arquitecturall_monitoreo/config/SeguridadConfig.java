package Ucentral.Arquitecturall_monitoreo.config;



import Ucentral.Arquitecturall_monitoreo.seguridad.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SeguridadConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SeguridadConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/usuarios/registrar",
                        "/api/usuarios/login",
                        "/api/usuarios/login-google",
                        "/ping",
                        "/api/usuarios/actualizar-contraseña",
                        "/api/detecciones/imagen"
                ).permitAll()
                .anyRequest().authenticated()
        );
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

