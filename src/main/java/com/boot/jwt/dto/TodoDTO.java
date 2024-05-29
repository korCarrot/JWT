package com.boot.jwt.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoDTO {

    private Long tno;
    private String title;

    //객체를 JSON으로 직렬화할 때 날짜 형식을 지정하는 데 사용
    //shape = JsonFormat.Shape.STRING: 날짜를 문자열로 직렬화 (원래 기본은 타임스탬프)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate dueDate;

    private String writer;
    private boolean complete;
}
