package com.learnkafka.libraryeventsproducer.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Book {
	@NotNull
	private Integer bookId;
	@NotBlank(message = "Book name is missing.")
	private String bookName;
	@NotBlank(message="bookAuthor is missing.")
	private String bookAuthor;

}
