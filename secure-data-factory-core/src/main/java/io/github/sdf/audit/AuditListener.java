package io.github.sdf.audit;

/**
 * Listener interface for receiving audit events.
 * Implement and register with {@link AuditLogger} to hook into the audit pipeline.
 */
@FunctionalInterface
public interface AuditListener {

    /**
     * Called whenever an auditable action occurs.
     *
     * @param event the audit event describing what happened
     */
    void onEvent(AuditEvent event);
}
