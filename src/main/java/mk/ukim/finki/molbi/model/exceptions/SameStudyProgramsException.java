package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FOUND)
public class SameStudyProgramsException extends RuntimeException {

    public SameStudyProgramsException() {
        super(String.format("The student has same old and new study programs"));
    }
}
