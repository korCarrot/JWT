package com.boot.jwt.repository;

import com.boot.jwt.domain.Todo;
import com.boot.jwt.repository.search.TodoSearch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoSearch {
}
