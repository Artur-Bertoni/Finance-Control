package com.financecontrol.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFeedbackRequest {
    private String  type;
    private String  message;
    private Integer npsScore;
}
