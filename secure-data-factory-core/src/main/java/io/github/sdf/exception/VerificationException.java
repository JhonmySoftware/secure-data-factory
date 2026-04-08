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
package io.github.sdf.exception;

/**
 * Exception thrown when cryptographic verification or policy validation fails.
 *
 * @author Jhon Quiñones Arboleda
 */
public class VerificationException extends RuntimeException {

    private final String verificationStep;

    public VerificationException(String message) {
        super(message);
        this.verificationStep = "unknown";
    }

    public VerificationException(String message, String verificationStep) {
        super(message);
        this.verificationStep = verificationStep;
    }

    public VerificationException(String message, Throwable cause) {
        super(message, cause);
        this.verificationStep = "unknown";
    }

    public VerificationException(String message, String verificationStep, Throwable cause) {
        super(message, cause);
        this.verificationStep = verificationStep;
    }

    /**
     * Returns the verification step where the failure occurred.
     */
    public String getVerificationStep() {
        return verificationStep;
    }
}
