package mk.ukim.finki.molbi.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.NOT_FOUND)
public class InstallmentPaymentRequestNotFoundException extends RuntimeException {
    public InstallmentPaymentRequestNotFoundException(Long id) {
        super("Installment Payment Student request not found with id " + id);
    }
}
