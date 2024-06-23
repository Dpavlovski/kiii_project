package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SessionStillActiveException extends RuntimeException {
    public SessionStillActiveException(Long id) {
        super("Session with id " + id + " is still active.");
    }
}