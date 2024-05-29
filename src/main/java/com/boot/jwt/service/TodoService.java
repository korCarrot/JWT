package com.boot.jwt.service;

import com.boot.jwt.dto.PageRequestDTO;
import com.boot.jwt.dto.PageResponseDTO;
import com.boot.jwt.dto.TodoDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TodoService {
    Long register(TodoDTO todoDTO);

    TodoDTO read(Long tno);

    PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO);

    void remove(Long tno);

    void modify(TodoDTO todoDTO);
}
