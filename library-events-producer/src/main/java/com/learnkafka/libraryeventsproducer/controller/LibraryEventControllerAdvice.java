package com.learnkafka.libraryeventsproducer.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LibraryEventControllerAdvice {
	
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleRequestBody(MethodArgumentNotValidException ex){
		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		String errorMessage=fieldErrors.stream()
			.map(fieldError -> fieldError.getField() + " - " + fieldError.getDefaultMessage())
			.sorted()
			.collect(Collectors.joining(" and "));
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
	}

}
