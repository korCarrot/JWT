package com.boot.jwt.repository.search;

import com.boot.jwt.domain.QTodo;
import com.boot.jwt.domain.Todo;
import com.boot.jwt.dto.PageRequestDTO;
import com.boot.jwt.dto.TodoDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

@Log4j2
public class TodoSearchImpl extends QuerydslRepositorySupport implements TodoSearch {

    public TodoSearchImpl() {
        super(Todo.class);
    }

    @Override
    public Page<TodoDTO> list(PageRequestDTO pageRequestDTO) {

        QTodo todo = QTodo.todo;

        JPQLQuery<Todo> query = from(todo);

        if (pageRequestDTO.getFrom() != null && pageRequestDTO.getTo() != null) {

            BooleanBuilder fromToBuilder = new BooleanBuilder();
            fromToBuilder.and(todo.dueDate.goe(pageRequestDTO.getFrom()));  //goe: Greater than or Equal
            fromToBuilder.and(todo.dueDate.loe(pageRequestDTO.getTo()));    //loe: Less than or Equal
            query.where(fromToBuilder);
        }

        if (pageRequestDTO.getCompleted() != null) {
            query.where(todo.complete.eq(pageRequestDTO.getCompleted()));
        }

        if (pageRequestDTO.getKeyword() != null) {
            query.where(todo.title.contains(pageRequestDTO.getKeyword()));
        }

        this.getQuerydsl().applyPagination(pageRequestDTO.getPageable("tno"), query);

        //Projections : Querydsl에서 제공하는 클래스로, 쿼리 결과를 특정 형식으로 변환하는 데 사용됩니다. 주로 DTO(Data Transfer Object)로 매핑하는 데 사용
        JPQLQuery<TodoDTO> dtoQuery = query.select(Projections.bean(TodoDTO.class,
                todo.tno,
                todo.title,
                todo.dueDate,
                todo.complete,
                todo.writer
        ));

        List<TodoDTO> list = dtoQuery.fetch();

        long count = dtoQuery.fetchCount();


        return new PageImpl<>(list, pageRequestDTO.getPageable("tno"), count);
    }
}