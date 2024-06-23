package mk.ukim.finki.molbi.model.exceptions;

import mk.ukim.finki.molbi.model.requests.StudentRequest;

public class RequestAlreadyProcessedOrAlreadyApprovedException extends RuntimeException {
    public RequestAlreadyProcessedOrAlreadyApprovedException(StudentRequest request) {
        super(request.getIsProcessed() ?
                "Request with id " + request.getId() + " is already processed" :
                "Request with id " + request.getId() + " is already approved");
    }
}
