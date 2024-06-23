package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotAuthorizedForThisActionException extends RuntimeException {

    public NotAuthorizedForThisActionException() {
        super("Not authorized for this action.");
    }
}
