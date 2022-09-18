package cybersoft.java18.exception;

public class DatabaseNotFoundException extends RuntimeException {
    public DatabaseNotFoundException(String message) {
        super(message);
    }
}
