package com.example;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	private RestTemplate restTemplate = new RestTemplate();

	@Bean
	public Function<Flux<String>, Flux<String>> recommendations() {
		return userIds -> userIds.flatMap(userId -> Flux.just(restTemplate.postForObject("http://localhost:8080/recommendations", userId, String.class)));
	}
}
