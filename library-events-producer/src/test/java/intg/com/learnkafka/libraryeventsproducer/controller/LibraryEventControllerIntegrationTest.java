package com.learnkafka.libraryeventsproducer.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.learnkafka.libraryeventsproducer.domain.Book;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}" })
public class LibraryEventControllerIntegrationTest {

	@Autowired
	TestRestTemplate restTemplate;
	
	@Autowired
	EmbeddedKafkaBroker embeddedKafka;
	
	private Consumer<Integer, String> consumer;
	
	@BeforeEach
	public void setUp() {
		Map<String, Object> configs=new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafka));
		consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
		embeddedKafka.consumeFromAllEmbeddedTopics(consumer);
	}
	
	@AfterEach
	public void tearDown() {
		consumer.close();
	}

	@Test
	@Timeout(1000)
	public void postLibraryEvent()  {
		// Given
		LibraryEvent libraryEven = LibraryEvent.builder().libraryEventId(null)
				.book(Book.builder().bookId(456).bookName("Spring kafka integration").bookAuthor("Vimal").build())
				.build();
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<LibraryEvent> entity = new HttpEntity<LibraryEvent>(libraryEven, headers);

		// When
		ResponseEntity<LibraryEvent> exchange = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, entity,
				LibraryEvent.class);

		// Then
		assertEquals(HttpStatus.CREATED, exchange.getStatusCode());
		ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
		String value = consumerRecord.value();
		assertEquals("{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Spring kafka integration\",\"bookAuthor\":\"Vimal\"}}", value);

	}

}
