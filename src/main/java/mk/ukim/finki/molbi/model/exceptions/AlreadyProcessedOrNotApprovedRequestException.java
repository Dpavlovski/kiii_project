package mk.ukim.finki.molbi.model.exceptions;

import mk.ukim.finki.molbi.model.requests.CourseGroupChangeStudentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class AlreadyProcessedOrNotApprovedRequestException extends RuntimeException {
    public AlreadyProcessedOrNotApprovedRequestException(CourseGroupChangeStudentRequest request) {
        super(request.getIsApproved() == null || !request.getIsApproved() ?
                "Request with id " + request.getId() + " is already processed" :
                "Request with id " + request.getId() + " is not approved");
    }

}
