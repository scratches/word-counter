package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class RecommendationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendationServiceApplication.class, args);
	}

	@Bean
	public Function<Flux<String>, Flux<List<Recommendation>>> recommendations() {
		return userIds -> userIds.map(userId -> getRecommendations(userId));
	}

	public List<Recommendation> getRecommendations(String userId) {
		return Arrays.asList(new Recommendation("foo"), new Recommendation("bar"));
	}
}
