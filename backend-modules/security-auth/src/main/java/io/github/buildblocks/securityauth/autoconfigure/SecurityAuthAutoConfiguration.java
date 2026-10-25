package io.github.buildblocks.securityauth.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.github.buildblocks.securityauth.controller.AuthController;
import io.github.buildblocks.securityauth.handler.AuthExceptionHandler;
import io.github.buildblocks.securityauth.jwt.JwtAuthenticationFilter;
import io.github.buildblocks.securityauth.jwt.JwtService;
import io.github.buildblocks.securityauth.security.ApiAccessDeniedHandler;
import io.github.buildblocks.securityauth.security.ApiAuthenticationEntryPoint;
import io.github.buildblocks.securityauth.service.AuthService;
import io.github.buildblocks.securityauth.user.AuthUserStore;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration(beforeName = {
        "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "buildblocks.security-auth",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(SecurityAuthProperties.class)
public class SecurityAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService jwtService(SecurityAuthProperties properties) {
        return new JwtService(
                properties.getJwt().getSecret(),
                properties.getJwt().getExpirationMinutes());
    }

    @Bean
    @ConditionalOnMissingBean(AuthService.class)
    public AuthService authService(ObjectProvider<AuthUserStore> userStoreProvider,
                                   PasswordEncoder passwordEncoder,
                                   JwtService jwtService) {
        AuthUserStore userStore = userStoreProvider.getIfAvailable();
        if (userStore == null) {
            throw new IllegalStateException(
                    "buildblocks security-auth needs an AuthUserStore bean. "
                            + "Create a class that implements AuthUserStore and annotate it with @Component.");
        }
        return new AuthService(userStore, passwordEncoder, jwtService);
    }

    @Bean
    @ConditionalOnMissingBean(AuthController.class)
    public AuthController authController(AuthService authService) {
        return new AuthController(authService);
    }

    @Bean
    @ConditionalOnMissingBean(AuthExceptionHandler.class)
    public AuthExceptionHandler authExceptionHandler() {
        return new AuthExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ApiAuthenticationEntryPoint.class)
    public ApiAuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new ApiAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ApiAccessDeniedHandler.class)
    public ApiAccessDeniedHandler apiAccessDeniedHandler(ObjectMapper objectMapper) {
        return new ApiAccessDeniedHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityAuthFilterChain(HttpSecurity http,
                                                       SecurityAuthProperties properties,
                                                       JwtService jwtService,
                                                       ApiAuthenticationEntryPoint entryPoint,
                                                       ApiAccessDeniedHandler accessDeniedHandler) throws Exception {

        String[] publicPaths = properties.getPublicPaths().toArray(String[]::new);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}