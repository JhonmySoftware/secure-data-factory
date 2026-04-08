/*
 * Copyright (c) 2024 Secure Data Factory Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sdf.audit;

/**
 * Listener interface for receiving audit events.
 * Implement and register with {@link AuditLogger} to hook into the audit pipeline.
 *
 * @author Jhon Quiñones Arboleda
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
