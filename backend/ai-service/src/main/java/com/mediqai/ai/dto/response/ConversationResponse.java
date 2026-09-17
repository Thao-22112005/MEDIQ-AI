package com.mediqai.ai.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConversationResponse {

    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}