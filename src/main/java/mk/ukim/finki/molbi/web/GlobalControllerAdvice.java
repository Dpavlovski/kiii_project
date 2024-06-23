package mk.ukim.finki.molbi.web;

import mk.ukim.finki.molbi.model.enums.RequestType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute
    public void globalModelAttributes(Model model) {
        RequestType[] values = RequestType.values();
        model.addAttribute("requestTypes", values);
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("username", name);

    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("exceptionMessage", ex.getMessage());
        return "error";
    }

}