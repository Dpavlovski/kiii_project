package mk.ukim.finki.molbi.repository;

import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface EmailRepository {
    void sendMail(String[] to, String subject, String template, List<String> cc, Map<String, Object> model, File attachment) throws MessagingException;

    @Async
    void sendMailAsync(String[] to, String subject, String template, List<String> cc, Map<String, Object> model, File attachment);
}
