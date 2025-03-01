package com.example.cards;

import com.example.cards.dto.CardsContactInfoDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableConfigurationProperties(value = {CardsContactInfoDto.class})
@OpenAPIDefinition(
		info = @Info(
				title = "Cards microservice REST API Documentation",
				description = "MyBank Loans microservice REST API Documentation",
				version = "v1",
				contact = @Contact(
						name = "Sayyed Haaris",
						email = "dummy@gmail.com",
						url = "https://github.com/Haaris-Sayyed"
				),
				license = @License(
						name = "Apache 2.0",
						url = "https://github.com/Haaris-Sayyed"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "MyBank Cards Microservice REST API Documentation",
				url = "https://github.com/Haaris-Sayyed"
		)
)
public class CardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardsApplication.class, args);
	}
}