package com.test.todo_list_backend.models.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TodosSummaryResponseDTO {

    private BigDecimal completedTodosPercentage;
    private BigDecimal inProgressTodosPercentage;
    private BigDecimal notStartedTodosPercentage;

}
