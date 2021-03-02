package com.libraryevent.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.libraryevent.service.LibraryEventService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventConsumerConfig {

	@Autowired
	private KafkaProperties properties;

	@Autowired
	private LibraryEventService libraryEventService;

	@Bean("kafkaListenerContainerFactory")
	ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory
				.getIfAvailable(() -> new DefaultKafkaConsumerFactory<>(this.properties.buildConsumerProperties())));
		factory.setConcurrency(3);
		// factory.getContainerProperties().setAckMode(AckMode.MANUAL);

		factory.setErrorHandler((thrownException, data) -> {
			log.info("Exception is consumer config {} and the record is {}", thrownException.getMessage(), data);
			// persist
		});

		factory.setRecoveryCallback(context -> {

			if (context.getLastThrowable().getCause() instanceof RecoverableDataAccessException) {
				// invoke recovery logic
				log.info("Inside recover section logic");
//				Arrays.asList(context.attributeNames()).stream().forEach(attribute -> {
//					System.out.println("Name : "+attribute +" , value : "+ context.getAttribute(attribute));
//				});
				@SuppressWarnings("unchecked")
				ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) context
						.getAttribute("record");
				libraryEventService.handleRecover(consumerRecord);

			} else {
				log.info("inside non recoverable logic");
				throw new RuntimeException(context.getLastThrowable().getMessage());
			}
			return null;
		});

		factory.setRetryTemplate(retryTemplate());

		return factory;
	}

	@Bean
	public RetryTemplate retryTemplate() {

		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(1000);

		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy());
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}

	@Bean
	public RetryPolicy retryPolicy() {
//		SimpleRetryPolicy policy = new SimpleRetryPolicy();
//		policy.setMaxAttempts(3);
		Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
		retryableExceptions.put(IllegalArgumentException.class, false);
		retryableExceptions.put(RecoverableDataAccessException.class, true);
		SimpleRetryPolicy policy = new SimpleRetryPolicy(3, retryableExceptions, true);
		return policy;
	}

}
