package com.enviro.assessment.junior.andries.model;

/**
 * Outcome of a withdrawal notice after business-rule validation.
 * Since invalid requests are rejected with a 4xx error and never persisted,
 * every notice that reaches the database is APPROVED - the status is kept
 * as an explicit column (rather than assumed) so the system can be
 * extended later (e.g. a manual-review / PENDING workflow) without a
 * schema change.
 */
public enum WithdrawalStatus {
    APPROVED,
    DECLINED
}
