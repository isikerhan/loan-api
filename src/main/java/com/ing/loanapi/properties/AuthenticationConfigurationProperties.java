package com.ing.loanapi.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthenticationConfigurationProperties(BasicAuthentication basic) {

	public record BasicAuthentication(List<User> users) {

		public record User(String username, String password, String[] roles) {
		}
	}
}
