package at.ac.tuwien.student.e11843614.exception;

public class TimeoutException extends Exception {

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeoutException(org.sat4j.specs.TimeoutException exception) {
        super(exception.getMessage().substring(1), exception);
    }

}
