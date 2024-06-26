package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChangeStudyProgramStudentRequestException extends RuntimeException {

    public ChangeStudyProgramStudentRequestException(String studyProgramCode) {
        super(String.format("The following study program code: %s does not exist", studyProgramCode));
    }
}
