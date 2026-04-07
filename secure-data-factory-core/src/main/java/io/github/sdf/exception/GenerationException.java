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
