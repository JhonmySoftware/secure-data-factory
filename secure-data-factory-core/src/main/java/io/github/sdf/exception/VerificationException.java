package io.github.sdf.exception;

/**
 * Exception thrown when cryptographic verification or policy validation fails.
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
