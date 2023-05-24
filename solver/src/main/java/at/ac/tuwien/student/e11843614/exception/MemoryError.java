package at.ac.tuwien.student.e11843614.exception;

public class MemoryError extends Error {

    public MemoryError(String message) {
        super(message);
    }

    public MemoryError(String message, Throwable cause) {
        super(message, cause);
    }

}
