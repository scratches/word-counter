package com.example;

import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
@SpringBootTest(properties="server.port=0", webEnvironment = WebEnvironment.NONE)
public class CounterApplicationTests {

	@LocalServerPort
	private int port;
	@Autowired
	private ObjectMapper mapper;
	private TestRestTemplate rest = new TestRestTemplate();
	@Autowired
	@Qualifier("count")
	private Function<Flux<String>, Flux<Map<String, Integer>>> count;
	@Autowired
	private Function<Flux<String>, Flux<String>> split;
	@Autowired
	@Qualifier("splitAndCount")
	private Function<Flux<String>, Flux<Map<String, Integer>>> splitAndCount;

	@Test
	public void count() {
		assertThat(count.apply(Flux.just("a", "b", "a")).blockFirst()).containsEntry("a",
				2);
	}

	@Test
	public void split() {
		assertThat(split.apply(Flux.just("a b c", "b d", "a")).collectList().block())
				.contains("a", "b");
	}

	@Test
	public void splitWithWhitespace() {
		assertThat(
				split.apply(Flux.just("a b c", "", "b d", "a")).collectList().block().size())
						.isEqualTo(6);
	}

	@Test
	public void splitWithPunctuation() {
		assertThat(
				split.apply(Flux.just("a, b c", "b,d", "a")).collectList().block().size())
						.isEqualTo(6);
	}

	@Test
	public void splitAndCount() {
		assertThat(splitAndCount.apply(Flux.just("a b c", "b d", "a")).blockFirst())
				.containsEntry("a", 2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitAndCountHttp() throws Exception {
		Map<String, Object> map = mapper.readValue(
				rest.postForObject("http://localhost:" + port + "/splitAndCount",
						"foo bar spam\nbar\nfoo\nbar", String.class),
				Map.class);
		assertThat(map).containsEntry("foo", 2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void countHttp() throws Exception {
		Map<String, Object> map = mapper
				.readValue(
						rest.postForObject("http://localhost:" + port + "/count",
								"\"foo\"\n\"bar\"\n\"foo\"\n\"bar\"", String.class),
						Map.class);
		assertThat(map).containsEntry("foo", 2);
	}

}
