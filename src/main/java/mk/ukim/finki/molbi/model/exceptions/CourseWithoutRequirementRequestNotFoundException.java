package mk.ukim.finki.molbi.model.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CourseWithoutRequirementRequestNotFoundException extends RuntimeException {
    public CourseWithoutRequirementRequestNotFoundException(Long id) {
        super("Course Without Requirement Request not found with id " + id);
    }
}
