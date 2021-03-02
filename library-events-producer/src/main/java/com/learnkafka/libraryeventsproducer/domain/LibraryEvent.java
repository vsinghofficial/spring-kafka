package com.learnkafka.libraryeventsproducer.domain;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LibraryEvent {
	private Integer libraryEventId;
	private LibraryEventType libraryEventType;
	@Valid
	private Book book;
}
