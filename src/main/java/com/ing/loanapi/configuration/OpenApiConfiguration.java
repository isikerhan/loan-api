package com.ing.loanapi.configuration;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@SecurityScheme(name = "basic", type = HTTP, scheme = "basic")
public class OpenApiConfiguration {

	@Bean
	public OpenAPI openAPI() {
		final var info = new Info()
				.title("Loan API")
				.description("Loan services")
				.version("1.0.0");

		return new OpenAPI()
				.info(info);
	}
}
