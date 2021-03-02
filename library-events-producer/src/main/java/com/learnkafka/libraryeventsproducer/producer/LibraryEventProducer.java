package com.learnkafka.libraryeventsproducer.producer;

import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventProducer {

	@Autowired
	private KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private String topic = "library-events";

	public void send(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		String data = objectMapper.writeValueAsString(libraryEvent);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, data);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, data, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, data, ex);
			}

		});
	}

	public ListenableFuture<SendResult<Integer, String>> sendApproch2(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		String data = objectMapper.writeValueAsString(libraryEvent);
		ProducerRecord<Integer, String> producerRecord = producerRecordBuilder(topic, key, data);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, data, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, data, ex);
			}

		});
		return listenableFuture;
	}

	private ProducerRecord<Integer, String> producerRecordBuilder(String topic, Integer key, String data) {
		List<Header> headers=List.of(new RecordHeader("event-source", "web".getBytes()));
		return new ProducerRecord<Integer, String>(topic,null, key, data,headers);
	}

	protected void handleFailure(Integer key, String data, Throwable ex) {
		log.error("Error while sending message : {}", ex.getLocalizedMessage());
	}

	private void handleSuccess(Integer key, String data, SendResult<Integer, String> result) {
		log.info("Message sent successfully for key: {} and value {} and partition is {}", key, data,
				result.getRecordMetadata().partition());

	}

}
