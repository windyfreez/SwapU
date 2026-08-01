package com.itsean.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionQueryDTO {

    private Integer page = 1;

    private Integer size = 20;
}
