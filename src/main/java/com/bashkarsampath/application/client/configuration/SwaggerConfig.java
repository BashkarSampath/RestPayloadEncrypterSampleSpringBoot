package com.bashkarsampath.application.client.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@NoArgsConstructor
@AllArgsConstructor
public class SwaggerConfig {
	@Value("${springdoc.info.name}")
	private String name;
	@Value("${springdoc.info.version}")
	private String version;
	@Value("${springdoc.info.contact.name}")
	private String contactName;
	@Value("${springdoc.info.contact.url}")
	private String contactUrl;
	@Value("${springdoc.info.contact.email}")
	private String contactEmail;
	@Value("${springdoc.info.description}")
	private String description;
	@Value("${springdoc.info.termsAndServiceUrl}")
	private String termsAndServiceUrl;

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo()).select()
				.apis(RequestHandlerSelectors.withClassAnnotation(SwaggerDocumentation.class)).build();
	}

	public ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(this.name).version(this.version)
				.contact(new Contact("email: " + this.contactName, this.contactUrl, this.contactEmail))
				.description(this.description).termsOfServiceUrl(this.termsAndServiceUrl).build();
	}
}