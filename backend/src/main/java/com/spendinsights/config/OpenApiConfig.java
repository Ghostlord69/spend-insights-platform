package com.spendinsights.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI spendInsightsOpenApi() {
		final String schemeName = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("Customer Spend Insights API")
						.description("JWT-secured REST APIs for ingesting customer spend/interaction events and querying analytics-ready spend trends.")
						.version("1.0.0")
						.contact(new Contact().name("Spend Insights").email("local@spendinsights.dev"))
						.license(new License().name("MIT")))
				.servers(List.of(new Server().url("/").description("Local")))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(new Components().addSecuritySchemes(schemeName,
						new SecurityScheme()
								.name(schemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
