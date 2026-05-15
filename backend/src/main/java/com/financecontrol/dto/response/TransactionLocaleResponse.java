package com.financecontrol.dto.response;

import com.financecontrol.entity.TransactionLocale;

public record TransactionLocaleResponse(
    Long id,
    String name,
    String address,
    String iconKey
) {
    public static TransactionLocaleResponse from(TransactionLocale tl) {
        return new TransactionLocaleResponse(tl.getId(), tl.getName(), tl.getAddress(), tl.getIconKey());
    }
}
