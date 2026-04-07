package io.github.sdf.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base contract for all data generators.
 *
 * @param <T> the type of object this generator produces
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
