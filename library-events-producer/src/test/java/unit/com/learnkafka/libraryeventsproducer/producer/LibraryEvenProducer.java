package com.learnkafka.libraryeventsproducer.producer;

import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.libraryeventsproducer.domain.Book;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;
import com.learnkafka.libraryeventsproducer.domain.LibraryEventType;

@ExtendWith(MockitoExtension.class)
public class LibraryEvenProducer {
	
	@InjectMocks
	LibraryEventProducer libraryEventProducer;
	
	@Mock
	KafkaTemplate<Integer, String> kafkaTemplate;
	
	@Spy
	ObjectMapper mapper=new ObjectMapper();
	
	@Test
	public void sendLibraryEvent_Failure() throws JsonProcessingException, InterruptedException, ExecutionException {
		
		//given
		LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).libraryEventType(LibraryEventType.NEW)
				.book(Book.builder().bookId(456).bookName("Spring kafka integration").bookAuthor("Vimal").build())
				.build();
		
		
		SettableListenableFuture<String> future=new SettableListenableFuture<>();
		future.setException(new RuntimeException("Exception calling kafka"));
		
		when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
		
		//when
		assertThrows(Exception.class, () -> libraryEventProducer.sendApproch2(libraryEvent).get());
		
		//then
		
			
	}
	
	
	@Test
	public void sendLibraryEvent_Success() throws JsonProcessingException, InterruptedException, ExecutionException {
		
		//given
		LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).libraryEventType(LibraryEventType.NEW)
				.book(Book.builder().bookId(456).bookName("Spring kafka integration").bookAuthor("Vimal").build())
				.build();
		
		
		SettableListenableFuture<SendResult<Integer, String>> future=new SettableListenableFuture<>();
		
		ProducerRecord<Integer, String> producerRecord= new ProducerRecord<Integer, String>("library-event", mapper.writeValueAsString(libraryEvent));
		RecordMetadata recordMetadata= new RecordMetadata(new TopicPartition("library-event", 3), 1, 1, System.currentTimeMillis(), 1232l, 432, 345);
		
		SendResult<Integer, String> sendResult=new SendResult<>(producerRecord, recordMetadata);
		
		future.set(sendResult);
		
		when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
		
		//when
		ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer.sendApproch2(libraryEvent);
		
		//then
		SendResult<Integer, String> sendResult2 = listenableFuture.get();
		assert sendResult2.getRecordMetadata().partition() ==3;
		
		
		
	}
	
	

}
