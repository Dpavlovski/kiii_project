package mk.ukim.finki.molbi.model.exceptions;

public class RequestsInRequestSessionException extends RuntimeException {
    public RequestsInRequestSessionException(Long id) {
        super("Cannot delete request session with ID " + id + " because it has associated entries in the table.");
    }
}
