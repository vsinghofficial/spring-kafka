package com.learnkafka.libraryeventsproducer.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.isA;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.libraryeventsproducer.domain.Book;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;
import com.learnkafka.libraryeventsproducer.producer.LibraryEventProducer;

@WebMvcTest(controllers = LibraryEventController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockBean
	LibraryEventProducer producer;
	
	
	@Test
	public void postLibraryEven() throws JsonProcessingException, Exception {
		//given
		LibraryEvent libraryEven = LibraryEvent.builder().libraryEventId(null)
				.book(Book.builder().bookId(456).bookName("Spring kafka integration").bookAuthor("Vimal").build())
				.build();
		
		when(producer.sendApproch2(isA(LibraryEvent.class))).thenReturn(null);
		
		//when
		mockMvc.perform(post("/v1/libraryevent")
					.content(objectMapper.writeValueAsString(libraryEven))
					.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isCreated());
			
		//then
	}
	
	@Test
	public void postLibraryEven_4xx() throws JsonProcessingException, Exception {
		//given
		LibraryEvent libraryEven = LibraryEvent.builder().libraryEventId(null)
				.book(new Book())
				.build();
		
		when(producer.sendApproch2(isA(LibraryEvent.class))).thenReturn(null);
		String expectedErrorMessage="book.bookAuthor - bookAuthor is missing. and book.bookId - must not be null and book.bookName - Book name is missing.";
		
		//when
		mockMvc.perform(post("/v1/libraryevent")
					.content(objectMapper.writeValueAsString(libraryEven))
					.contentType(MediaType.APPLICATION_JSON_VALUE))
					.andExpect(status().is4xxClientError())
					.andExpect(content().string(expectedErrorMessage));
			
		//then
	}

}
