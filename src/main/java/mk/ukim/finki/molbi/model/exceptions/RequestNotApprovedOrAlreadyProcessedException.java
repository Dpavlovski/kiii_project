package mk.ukim.finki.molbi.model.exceptions;

import mk.ukim.finki.molbi.model.requests.StudentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class RequestNotApprovedOrAlreadyProcessedException extends RuntimeException {
    public RequestNotApprovedOrAlreadyProcessedException(StudentRequest request) {
        super(request.getIsApproved() == null || !request.getIsApproved() ?
                "Request with id " + request.getId() + " is not approved" :
                "Request with id " + request.getId() + " is already processed");
    }


}
