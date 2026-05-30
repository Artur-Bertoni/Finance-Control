package com.financecontrol.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserFeedbackResponse {
    private Long          id;
    private String        userName;
    private String        userEmail;
    private String        type;
    private String        message;
    private Integer       npsScore;
    private LocalDateTime createdAt;
}
