package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class CounterApplication {
	
	@Bean
	// TODO: support Mono as return value
	public Function<Flux<String>, Flux<Map<String, Integer>>> count() {
		return words -> Flux.from(words.reduce(new HashMap<String, Integer>(),
				this::incrementWordCount));
	}
	
	@Bean
	public Function<Flux<String>, Flux<String>> split() {
		return line -> line.flatMap(value -> Flux.fromArray(value.split("\\s")));
	}

	@Bean
	public Function<Flux<String>, Flux<Map<String, Integer>>> splitAndCount() {
		return split().andThen(count());
	}

	public Map<String, Integer> incrementWordCount(Map<String, Integer> map,
			String word) {
		if (map.get(word) == null) {
			map.put(word, 1);
		}
		else {
			map.put(word, map.get(word) + 1);
		}
		return map;
	}

	public static void main(String[] args) {
		SpringApplication.run(CounterApplication.class, args);
	}
}
