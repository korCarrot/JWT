package com.boot.jwt.repository.search;

import com.boot.jwt.dto.PageRequestDTO;
import com.boot.jwt.dto.TodoDTO;
import org.springframework.data.domain.Page;

public interface TodoSearch {

    Page<TodoDTO> list(PageRequestDTO pageRequestDTO);

}
