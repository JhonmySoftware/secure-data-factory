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
package io.github.sdf.annotation;

import io.github.sdf.generator.DataGenerator;

import java.lang.annotation.*;

/**
 * Marks a field to be generated with synthetic data.
 *
 * <p>Apply this annotation to fields in your model classes to automatically
 * generate synthetic values during data creation.</p>
 *
 * <pre>{@code
 * public class Person {
 *     @SdfField(type = DataType.FIRST_NAME)
 *     private String firstName;
 *
 *     @SdfField(type = DataType.EMAIL)
 *     private String email;
 *
 *     @SdfField(ignore = true)
 *     private String internalId;
 * }
 * }</pre>
 *
 * @author Jhon Quiñones Arboleda
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface SdfField {

    /**
     * The type of data to generate for this field.
     */
    DataType value() default DataType.AUTO;

    /**
     * Locale for localized generation (e.g., "en_US", "es_CO").
     * Use "default" for the system's default locale.
     */
    String locale() default "default";

    /**
     * For phone numbers, specify country code (US, UK, CO, etc.).
     */
    String country() default "US";

    /**
     * Whether to apply masking to the generated value.
     */
    boolean mask() default false;

    /**
     * Whether to ignore this field during generation.
     * Useful for internal IDs or fields populated externally.
     */
    boolean ignore() default false;

    /**
     * Custom format pattern (used by some DataTypes).
     * Format syntax depends on the specific DataType.
     */
    String format() default "";

    /**
     * Minimum value for numeric fields.
     */
    double min() default 0;

    /**
     * Maximum value for numeric fields.
     */
    double max() default 100;

    /**
     * Fixed value to use instead of generating.
     * When set, the field will be set to this constant value.
     */
    String fixedValue() default "";
}