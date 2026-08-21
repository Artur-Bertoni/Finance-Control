package com.financecontrol.dto.response;

public record BulkDeletePreviewResponse(int items,
                                        long deletedTransactions,
                                        long unlinkedAccounts,
                                        long unlinkedTransactions) {

    public static BulkDeletePreviewResponse empty() {
        return new BulkDeletePreviewResponse(0, 0, 0, 0);
    }

    public static BulkDeletePreviewResponse plain(int items) {
        return new BulkDeletePreviewResponse(items, 0, 0, 0);
    }
}
