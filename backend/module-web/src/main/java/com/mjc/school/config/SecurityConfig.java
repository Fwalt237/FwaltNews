package com.mjc.school.config;

import com.mjc.school.service.security.MyUserDetailsService;
import com.mjc.school.service.security.jwt.JwtAuthenticationFilter;
import com.mjc.school.service.security.oauth2.MyOAuth2UserService;
import com.mjc.school.service.security.oauth2.OAuth2AuthenticationSuccessHandler;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;
    private final MyOAuth2UserService myOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(MyUserDetailsService myUserDetailsService,
                          MyOAuth2UserService myOAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          PasswordEncoder passwordEncoder){
        this.myUserDetailsService=myUserDetailsService;
        this.myOAuth2UserService=myOAuth2UserService;
        this.oauth2AuthenticationSuccessHandler=oauth2AuthenticationSuccessHandler;
        this.jwtAuthenticationFilter=jwtAuthenticationFilter;
        this.passwordEncoder=passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider  authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(myUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http.
                cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Unauthorized access - please login\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v*/auth/**").permitAll()
                    .requestMatchers("/oauth2/**").permitAll()
                    .requestMatchers("/swagger-ui/**","/swagger-resources/**","/v*/api-docs").permitAll()
                    .requestMatchers("/actuator/**").permitAll()

                    .requestMatchers(HttpMethod.GET,"/api/v*/news/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/v*/authors/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/v*/tags/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/v*/comments/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/v*/ai/**").permitAll()

                    .requestMatchers(HttpMethod.POST,"/api/v*/news/**").hasAnyRole("USER","ADMIN")
                    .requestMatchers(HttpMethod.POST,"/api/v*/comments/**").hasAnyRole("USER","ADMIN")
                    .requestMatchers(HttpMethod.POST,"/api/v*/ai/**").hasAnyRole("USER","ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/api/v*/ai/**").hasAnyRole("USER","ADMIN")

                    .requestMatchers(HttpMethod.POST,"/api/v*/authors/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,"/api/v*/tags/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH,"/api/v*/*/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,"/api/v*/*/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/api/v*/*/**").hasRole("ADMIN")

                .anyRequest().authenticated()
                )
                        .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(myOAuth2UserService))
                                .successHandler(oauth2AuthenticationSuccessHandler))
                        .authenticationProvider(authenticationProvider())
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Content-Type","x-auth-token","Accept"));
        configuration.setExposedHeaders(Arrays.asList("Authorization","x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
