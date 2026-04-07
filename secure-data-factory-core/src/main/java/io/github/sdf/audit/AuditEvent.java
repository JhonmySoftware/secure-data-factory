package io.github.sdf.audit;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable record of a single auditable action performed by the factory.
 */
public final class AuditEvent {

    /** Type of auditable action. */
    public enum EventType {
        DATA_GENERATED,
        DATA_ENCRYPTED,
        DATA_DECRYPTED,
        DATA_ANONYMIZED,
        POLICY_APPLIED,
        POLICY_VIOLATION,
        KEY_GENERATED,
        AUDIT_STARTED,
        AUDIT_STOPPED
    }

    private final String          eventId;
    private final EventType       eventType;
    private final Instant         timestamp;
    private final String          actorId;
    private final String          description;
    private final Map<String, Object> metadata;

    private AuditEvent(Builder builder) {
        this.eventId     = builder.eventId != null ? builder.eventId : UUID.randomUUID().toString();
        this.eventType   = builder.eventType;
        this.timestamp   = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.actorId     = builder.actorId;
        this.description = builder.description;
        this.metadata    = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getEventId()                { return eventId; }
    public EventType getEventType()           { return eventType; }
    public Instant getTimestamp()             { return timestamp; }
    public String getActorId()                { return actorId; }
    public String getDescription()            { return description; }
    public Map<String, Object> getMetadata()  { return metadata; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder(EventType type) {
        return new Builder(type);
    }

    public static final class Builder {
        private String eventId;
        private final EventType eventType;
        private Instant timestamp;
        private String actorId = "system";
        private String description = "";
        private final Map<String, Object> metadata = new HashMap<>();

        private Builder(EventType eventType) {
            this.eventType = eventType;
        }

        public Builder eventId(String eventId)        { this.eventId = eventId; return this; }
        public Builder timestamp(Instant ts)           { this.timestamp = ts; return this; }
        public Builder actorId(String actorId)         { this.actorId = actorId; return this; }
        public Builder description(String desc)        { this.description = desc; return this; }
        public Builder metadata(String key, Object val){ this.metadata.put(key, val); return this; }

        public AuditEvent build() {
            if (eventType == null) throw new IllegalStateException("eventType is required");
            return new AuditEvent(this);
        }
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
                "eventId='" + eventId + '\'' +
                ", type=" + eventType +
                ", timestamp=" + timestamp +
                ", actor='" + actorId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
