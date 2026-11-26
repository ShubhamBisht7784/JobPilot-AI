
package com.jobpilot.approval.event;

import com.jobpilot.approval.entity.ApprovalStatus;

public record ApprovalDecisionEvent(
        Long approvalId,
        ApprovalStatus decision
) {
}
