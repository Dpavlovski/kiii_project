package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CourseGroupChangeStudentRequestNotFoundException extends RuntimeException {
    public CourseGroupChangeStudentRequestNotFoundException(Long id) {
        super(String.format("Course group change student request with this id: %d was not found!", id));
    }

}
