package com.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import com.example.CounterApplication.WordCountProperties;

import reactor.core.publisher.Flux;

@SpringBootApplication
@EnableConfigurationProperties(WordCountProperties.class)
public class CounterApplication {

	@ConfigurationProperties("words")
	public class WordCountProperties {

		private Resource location = new FileSystemResource("/tmp/words.txt");

		public void setLocation(Resource location) {
			this.location = location;
		}

		public Resource getLocation() {
			return this.location;
		}

	}

	@Autowired
	private WordCountProperties words;
	
	@Bean
	public CommandLineRunner runner() {
		return args -> wordCount().run();
	}

	@Bean
	public Supplier<Flux<Object>> words() {
		return () -> {
			return Flux.intervalMillis(1000L).map(tick -> "one two two three three three");
		};
	}

	@Bean
	public Runnable wordCount() {
		return () -> splitAndCount().apply(resourceSupplier().get())
				.subscribe(printCounts());
	}

	@Bean
	public Consumer<Map<String, Integer>> printCounts() {
		return map -> System.err.println(map);
	}

	@Bean
	public Consumer<Object> print() {
		return o -> System.err.println(o);
	}

	@Bean
	public Supplier<Flux<String>> resourceSupplier() {
		Resource resource = words.getLocation();
		return () -> {
			try {
				return Flux.just(FileCopyUtils
						.copyToString(new InputStreamReader(resource.getInputStream())));
			}
			catch (IOException e) {
				throw new IllegalStateException("Cannot squirt file", e);
			}
		};
	}

	@Bean
	// TODO: support Mono as return value
	public Function<Flux<String>, Flux<Map<String, Integer>>> count() {
		return words -> Flux.from(
				words.reduce(new HashMap<String, Integer>(), this::incrementWordCount)
						.map(this::sort));
	}

	@Bean
	public Function<Flux<String>, Flux<String>> split() {
		return line -> line.flatMap(value -> Flux.fromArray(value.split("\\W")))
				.filter(value -> value.length() > 0);
	}

	@Bean
	public Function<Flux<String>, Flux<Map<String, Integer>>> splitAndCount() {
		return split().andThen(count());
	}

	@Bean
	public Function<Flux<String>, Flux<Map<String, Integer>>> splitWindowAndCount() {
		return input -> input.window(10).log().flatMap(splitAndCount()).log();
	}

	private Map<String, Integer> sort(Map<String, Integer> map) {
		return map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(u, v) -> u, LinkedHashMap::new));
	}

	public Map<String, Integer> incrementWordCount(Map<String, Integer> map,
			String word) {
		word = word.trim();
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
