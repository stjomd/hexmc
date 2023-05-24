package at.ac.tuwien.student.e11843614.exception;

public class OverflowException extends RuntimeException {

    public OverflowException(String message) {
        super(message);
    }

    public OverflowException(String message, Throwable cause) {
        super(message, cause);
    }

}
