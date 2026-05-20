package com.financecontrol.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ChangeGroupResponse(
    String groupId,
    LocalDateTime changedAt,
    boolean creation,
    boolean passwordChange,
    List<FieldChangeResponse> changes
) {}
