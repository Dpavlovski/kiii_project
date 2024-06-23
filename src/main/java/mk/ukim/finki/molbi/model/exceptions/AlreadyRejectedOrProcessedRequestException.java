package mk.ukim.finki.molbi.model.exceptions;

import mk.ukim.finki.molbi.model.requests.CourseGroupChangeStudentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class AlreadyRejectedOrProcessedRequestException extends RuntimeException {
    public AlreadyRejectedOrProcessedRequestException(CourseGroupChangeStudentRequest request) {
        super(request.getIsProcessed() ?
                "Request with id " + request.getId() + " is already rejected" :
                "Request with id " + request.getId() + " is already processed");
    }

}
