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
 * Exception thrown when data generation fails.
 */
public class GenerationException extends RuntimeException {

    private final String generatorType;

    public GenerationException(String message) {
        super(message);
        this.generatorType = "unknown";
    }

    public GenerationException(String message, String generatorType) {
        super(message);
        this.generatorType = generatorType;
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
        this.generatorType = "unknown";
    }

    public GenerationException(String message, String generatorType, Throwable cause) {
        super(message, cause);
        this.generatorType = generatorType;
    }

    /**
     * Returns the type of generator that triggered the exception.
     */
    public String getGeneratorType() {
        return generatorType;
    }
}
