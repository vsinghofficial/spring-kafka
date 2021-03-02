package com.libraryevent.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.libraryevent.service.LibraryEventService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventConsumer {
	
	@Autowired
	private LibraryEventService service;

	@KafkaListener(topics = { "library-events" }, containerFactory = "kafkaListenerContainerFactory")
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonMappingException, JsonProcessingException {
		log.info("stated consumer record : {}" + consumerRecord);
		service.processLibraryEvent(consumerRecord);
		log.info("completed consumer record : {}",consumerRecord);
	}

}
