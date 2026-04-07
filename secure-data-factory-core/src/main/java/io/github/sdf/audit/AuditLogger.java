package io.github.sdf.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central audit logger for the Secure Data Factory.
 *
 * <p>Records all security-relevant events and dispatches them to registered
 * {@link AuditListener} instances. Thread-safe.</p>
 *
 * <pre>{@code
 * AuditLogger logger = new AuditLogger("my-service");
 * logger.addListener(event -> System.out.println(event));
 * logger.log(AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
 *         .description("Generated 100 Person records")
 *         .build());
 * }</pre>
 */
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final String                         serviceId;
    private final Queue<AuditEvent>              eventBuffer;
    private final List<AuditListener>            listeners;
    private final int                            maxBufferSize;
    private       boolean                        enabled;

    public AuditLogger(String serviceId) {
        this(serviceId, 10_000);
    }

    public AuditLogger(String serviceId, int maxBufferSize) {
        this.serviceId     = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.maxBufferSize = maxBufferSize;
        this.eventBuffer   = new ConcurrentLinkedQueue<>();
        this.listeners     = new CopyOnWriteArrayList<>();
        this.enabled       = true;
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    /**
     * Records an audit event. If the buffer is full the oldest event is dropped.
     *
     * @param event the event to record
     */
    public void log(AuditEvent event) {
        if (!enabled || event == null) return;

        // Drop oldest if buffer is full
        while (eventBuffer.size() >= maxBufferSize) {
            eventBuffer.poll();
        }

        eventBuffer.add(event);
        log.debug("AUDIT [{}] {} — {}", event.getEventType(), event.getActorId(), event.getDescription());

        // Notify listeners
        for (AuditListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.warn("AuditListener threw an exception: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Convenience method: creates and logs an event in one call.
     */
    public void log(AuditEvent.EventType type, String description) {
        log(AuditEvent.builder(type)
                .actorId(serviceId)
                .description(description)
                .build());
    }

    // ── Listener management ───────────────────────────────────────────────────

    public void addListener(AuditListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(AuditListener listener) {
        listeners.remove(listener);
    }

    // ── Querying ──────────────────────────────────────────────────────────────

    /**
     * Returns an unmodifiable snapshot of all buffered events.
     */
    public List<AuditEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(eventBuffer));
    }

    /**
     * Returns all buffered events of the given type.
     */
    public List<AuditEvent> getEventsByType(AuditEvent.EventType type) {
        List<AuditEvent> result = new ArrayList<>();
        for (AuditEvent e : eventBuffer) {
            if (e.getEventType() == type) result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    /** Clears the in-memory event buffer. */
    public void clearBuffer() {
        eventBuffer.clear();
    }

    public int getBufferSize()  { return eventBuffer.size(); }
    public boolean isEnabled()  { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getServiceId(){ return serviceId; }
}
