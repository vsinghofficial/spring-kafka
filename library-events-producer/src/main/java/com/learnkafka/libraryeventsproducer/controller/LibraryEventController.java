package com.learnkafka.libraryeventsproducer.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;
import com.learnkafka.libraryeventsproducer.domain.LibraryEventType;
import com.learnkafka.libraryeventsproducer.producer.LibraryEventProducer;

@RestController
public class LibraryEventController {
	
	@Autowired
	private LibraryEventProducer libraryEventProducer;

	@PostMapping(path = "v1/libraryevent")
	public ResponseEntity<LibraryEvent> post(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
		//libraryEventProducer.send(libraryEvent);
		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		libraryEventProducer.sendApproch2(libraryEvent);
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}

	@PutMapping("v1/libraryevent")
	public ResponseEntity<?> put(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
		if(libraryEvent.getLibraryEventId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event id is missing");
		}
		
		libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
		libraryEventProducer.sendApproch2(libraryEvent);
		return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
	}

}
