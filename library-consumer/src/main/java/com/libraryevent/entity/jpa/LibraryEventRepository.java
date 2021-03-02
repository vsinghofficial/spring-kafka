package com.libraryevent.entity.jpa;

import org.springframework.data.repository.CrudRepository;

import com.libraryevent.entity.LibraryEvent;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent, Integer> {

}
