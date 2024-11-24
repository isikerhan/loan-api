package com.ing.loanapi.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.ing.loanapi.properties.AuthenticationConfigurationProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthenticationConfigurationProperties.class)
@EnableMethodSecurity
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/v3/api-docs/**").permitAll()
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers("/loans/**").authenticated()
						.anyRequest().denyAll())
				.httpBasic(withDefaults());

		return httpSecurity.build();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService(
			PasswordEncoder passwordEncoder,
			AuthenticationConfigurationProperties authenticationConfigurationProperties) {

		final var users = authenticationConfigurationProperties.basic().users().stream()
				.map(user -> User.builder()
						.username(user.username())
						.password(passwordEncoder.encode(user.password()))
						.roles(user.roles())
						.build())
				.toArray(UserDetails[]::new);

		return new InMemoryUserDetailsManager(users);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
