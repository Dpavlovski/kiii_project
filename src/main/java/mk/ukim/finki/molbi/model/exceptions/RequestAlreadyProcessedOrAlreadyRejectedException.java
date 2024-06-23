package mk.ukim.finki.molbi.model.exceptions;

import mk.ukim.finki.molbi.model.requests.StudentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class RequestAlreadyProcessedOrAlreadyRejectedException extends RuntimeException {
    public RequestAlreadyProcessedOrAlreadyRejectedException(StudentRequest request) {
        super(request.getIsProcessed() ?
                "Request with id " + request.getId() + " is already processed" :
                "Request with id " + request.getId() + " is already rejected");
    }


}
