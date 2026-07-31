package com.itsean.swapu_admin.dto;

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
