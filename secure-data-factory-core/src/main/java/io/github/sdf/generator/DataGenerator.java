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
package io.github.sdf.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base contract for all data generators.
 *
 * @param <T> the type of object this generator produces
 * @author Jhon Quiñones Arboleda
 */
public interface DataGenerator<T> {

    /**
     * Generates a single instance of {@code T}.
     *
     * @return a freshly generated object; never {@code null}
     */
    T generate();

    /**
     * Generates {@code count} instances of {@code T}.
     *
     * @param count number of objects to generate (must be &gt; 0)
     * @return an unmodifiable list of generated objects
     * @throws IllegalArgumentException if {@code count} is not positive
     */
    default List<T> generate(int count) {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0, was: " + count);
        List<T> generated = new ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            generated.add(generate());
        }
        return Collections.unmodifiableList(generated);
    }

    /**
     * Returns a human-readable name for this generator (used in logs and audit events).
     */
    String getGeneratorName();
}
