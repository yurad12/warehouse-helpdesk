package com.skala.warehouse_helpdesk.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.domain.Ticket;

@Aspect
@Component
public class ToolAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(ToolAuditAspect.class);

    private final WarehouseAiProperties properties;

    public ToolAuditAspect(WarehouseAiProperties properties) {
        this.properties = properties;
    }

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        String toolName = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "#" + joinPoint.getSignature().getName();

        ToolAuditContext.markToolUsed();

        try {
            Object result = joinPoint.proceed();
            logSuccess(toolName, elapsedMs(startedAt), result);
            return result;
        }
        catch (Throwable ex) {
            log.warn("tool={} userId={} warehouseId={} status=FAIL elapsedMs={}",
                    toolName,
                    properties.testUser().userId(),
                    properties.testUser().warehouseId(),
                    elapsedMs(startedAt));
            throw ex;
        }
    }

    private void logSuccess(String toolName, long elapsedMs, Object result) {
        if (result instanceof Ticket ticket) {
            log.info("tool={} userId={} warehouseId={} status=OK elapsedMs={} ticketNo={}",
                    toolName,
                    properties.testUser().userId(),
                    properties.testUser().warehouseId(),
                    elapsedMs,
                    ticket.ticketNo());
            return;
        }

        log.info("tool={} userId={} warehouseId={} status=OK elapsedMs={}",
                toolName,
                properties.testUser().userId(),
                properties.testUser().warehouseId(),
                elapsedMs);
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
