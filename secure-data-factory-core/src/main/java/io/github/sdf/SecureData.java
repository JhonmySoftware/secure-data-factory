package io.github.sdf;

import io.github.sdf.audit.AuditEvent;
import io.github.sdf.crypto.SecurityLevel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Wraps a generated data record with its associated security metadata.
 *
 * <p>Returned by {@link SecureDataFactory} for every generation operation.
 * Provides access to the data, the applied security level, the audit events
 * recorded during generation, and a creation timestamp.</p>
 *
 * @param <T> the type of the wrapped data object
 */
public final class SecureData<T> {

    private final T               data;
    private final SecurityLevel   securityLevel;
    private final Instant         createdAt;
    private final List<AuditEvent> auditTrail;
    private final String          checksum;

    private SecureData(Builder<T> builder) {
        this.data          = Objects.requireNonNull(builder.data, "data must not be null");
        this.securityLevel = builder.securityLevel;
        this.createdAt     = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.auditTrail    = builder.auditTrail != null
                             ? Collections.unmodifiableList(builder.auditTrail)
                             : Collections.emptyList();
        this.checksum      = builder.checksum;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Returns the wrapped data object. */
    public T getData() { return data; }

    /** Returns the security level applied during generation. */
    public SecurityLevel getSecurityLevel() { return securityLevel; }

    /** Returns the instant when this wrapper was created. */
    public Instant getCreatedAt() { return createdAt; }

    /**
     * Returns the ordered list of audit events recorded during the generation
     * of this record. Empty if no audit trail was captured.
     */
    public List<AuditEvent> getAuditTrail() { return auditTrail; }

    /** Returns the optional integrity checksum of the wrapped data. */
    public String getChecksum() { return checksum; }

    /** Returns {@code true} if an audit trail was captured for this record. */
    public boolean hasAuditTrail() { return !auditTrail.isEmpty(); }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static <T> Builder<T> builder(T data) {
        return new Builder<>(data);
    }

    public static final class Builder<T> {
        private final T               data;
        private SecurityLevel         securityLevel = SecurityLevel.MEDIUM;
        private Instant               createdAt;
        private List<AuditEvent>      auditTrail;
        private String                checksum;

        private Builder(T data) {
            this.data = data;
        }

        public Builder<T> securityLevel(SecurityLevel level)   { this.securityLevel = level; return this; }
        public Builder<T> createdAt(Instant createdAt)         { this.createdAt = createdAt; return this; }
        public Builder<T> auditTrail(List<AuditEvent> events)  { this.auditTrail = events;  return this; }
        public Builder<T> checksum(String checksum)            { this.checksum = checksum;  return this; }

        public SecureData<T> build() { return new SecureData<>(this); }
    }

    @Override
    public String toString() {
        return "SecureData{level=" + securityLevel +
               ", createdAt=" + createdAt +
               ", auditEvents=" + auditTrail.size() +
               ", data=" + data + "}";
    }
}
