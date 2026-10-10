package com.skala.warehouse_helpdesk.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.skala.warehouse_helpdesk.tool.PalletTools;

@SpringBootTest
class ToolAuditAspectTests {

    private final PalletTools palletTools;

    @Autowired
    ToolAuditAspectTests(PalletTools palletTools) {
        this.palletTools = palletTools;
    }

    @AfterEach
    void tearDown() {
        ToolAuditContext.clear();
    }

    @Test
    void marksToolUsedWhenToolMethodRuns() {
        ToolAuditContext.start();

        palletTools.getPalletStatus("PALLET-123");

        assertThat(ToolAuditContext.wasToolUsed()).isTrue();
    }
}
