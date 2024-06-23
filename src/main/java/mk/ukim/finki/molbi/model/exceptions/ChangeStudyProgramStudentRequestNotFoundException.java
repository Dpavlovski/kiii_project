package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChangeStudyProgramStudentRequestNotFoundException extends RuntimeException {

    public ChangeStudyProgramStudentRequestNotFoundException(Long id) {
        super(String.format("The change study program student request with this id: %d was not found!", id));
    }
}
