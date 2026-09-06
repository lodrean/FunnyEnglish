package com.sotospeak.config

import com.sotospeak.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity, corsConfigurationSource: CorsConfigurationSource): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints (paths without /api context path)
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // Live guest endpoints: /public/speaking/**, /public/guest-events, /public/logs.
                    // (bd 8zm, 2026-09-06: legacy /public/tests, /public/adaptive удалены вместе
                    // с контроллерами — сужение /public/** больше не требуется.)
                    .requestMatchers("/public/**").permitAll()

                    // Admin endpoints (hasAuthority because JwtAuthFilter adds ROLE_ prefix)
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                // Анонимный доступ к защищённому пути → 401 (bd FunnyEnglish-nj2.7, вместо прежнего 403).
                // Истёкший токен (attribute ставит JwtAuthenticationFilter) → 401 с code=TOKEN_EXPIRED —
                // сигнал клиенту сделать refresh. Аутентифицированному без нужной роли по-прежнему
                // отдаёт 403 AccessDeniedHandler (не этот entry point).
                ex.authenticationEntryPoint { request, response, _ ->
                    response.status = jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = "application/json"
                    if (request.getAttribute(JwtAuthenticationFilter.ATTR_TOKEN_EXPIRED) == true) {
                        response.writer.write("""{"error":"Token expired","code":"TOKEN_EXPIRED"}""")
                    } else {
                        response.writer.write("""{"error":"Unauthorized","code":"UNAUTHORIZED"}""")
                    }
                }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun webSecurityCustomizer(): org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer {
        return org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers("/actuator/health")
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}
