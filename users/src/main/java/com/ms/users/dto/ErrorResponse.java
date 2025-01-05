package com.ms.users.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
}
