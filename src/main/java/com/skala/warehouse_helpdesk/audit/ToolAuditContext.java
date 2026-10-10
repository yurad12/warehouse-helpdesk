package com.skala.warehouse_helpdesk.audit;

public final class ToolAuditContext {

    private static final ThreadLocal<Boolean> TOOL_USED = ThreadLocal.withInitial(() -> false);

    private ToolAuditContext() {
    }

    public static void start() {
        TOOL_USED.set(false);
    }

    public static void markToolUsed() {
        TOOL_USED.set(true);
    }

    public static boolean wasToolUsed() {
        return TOOL_USED.get();
    }

    public static void clear() {
        TOOL_USED.remove();
    }
}
