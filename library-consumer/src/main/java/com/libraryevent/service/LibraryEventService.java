package com.libraryevent.service;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryevent.entity.LibraryEvent;
import com.libraryevent.entity.jpa.LibraryEventRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LibraryEventService {

	LibraryEventRepository repository;

	@Autowired
	ObjectMapper mapper;
	
	@Autowired
	private KafkaTemplate<Integer, String> kafkaTemplate;

	public LibraryEventService(LibraryEventRepository repository) {
		this.repository = repository;
	}

	public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord)
			throws JsonMappingException, JsonProcessingException {
		LibraryEvent libraryEvent = mapper.readValue(consumerRecord.value(), LibraryEvent.class);

		if (libraryEvent.getLibraryEventId() != null && libraryEvent.getLibraryEventId() == 000) {
			throw new RecoverableDataAccessException("Temporary network issue");
		}

		switch (libraryEvent.getLibraryEventType()) {
		case NEW:
			// save operation
			save(libraryEvent);
			break;
		case UPDATE:
			// update operation
			validate(libraryEvent);
			save(libraryEvent);
			break;
		default:
			log.info("Invalid library event type.");
		}

	}

	private void validate(LibraryEvent libraryEvent) {
		if (libraryEvent.getLibraryEventId() == null) {
			throw new IllegalArgumentException("Library Event id is missing");
		}

		Optional<LibraryEvent> findById = repository.findById(libraryEvent.getLibraryEventId());
		if (findById.isEmpty()) {
			throw new IllegalArgumentException("Not a valid library id");
		}
		log.info("Validation is successful.");

	}

	private void save(LibraryEvent libraryEvent) {
		libraryEvent.getBook().setLibraryEvent(libraryEvent);
		repository.save(libraryEvent);
		log.info("Successfully persisted.");
	}
	
	public void handleRecover(ConsumerRecord<Integer, String> consumerRecord) {
		Integer key = consumerRecord.key();
		String value = consumerRecord.value();
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

		});
	}
	
	protected void handleFailure(Integer key, String data, Throwable ex) {
		log.error("Error while sending message : {}", ex.getLocalizedMessage());
	}

	private void handleSuccess(Integer key, String data, SendResult<Integer, String> result) {
		log.info("Message sent successfully for key: {} and value {} and partition is {}", key, data,
				result.getRecordMetadata().partition());

	}

}
