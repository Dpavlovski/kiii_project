package mk.ukim.finki.molbi.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.config.MailConfig;
import mk.ukim.finki.molbi.repository.EmailRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class EmailRepositoryImpl implements EmailRepository {

    private final MailConfig mailConfig;
    private DevEmailRepositoryImpl devEmailRepository;

    @Override
    public void sendMail(String[] to, String subject, String template, List<String> cc, Map<String, Object> model, File attachment) {
        try {
            final MimeMessage mimeMessage = mailConfig.mailSender().createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);

            final Context ctx = new Context(Locale.getDefault());
            ctx.setVariables(model);
            final String htmlContent = mailConfig.emailTemplateEngine().process(template, ctx);
            message.setText(htmlContent, true);

            if (attachment != null) {
                FileSystemResource file = new FileSystemResource(attachment);
                message.addAttachment(Objects.requireNonNull(file.getFilename()), file);
            }

            mailConfig.mailSender().send(mimeMessage);
            devEmailRepository.saveMailToDisk(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("Mail not sent: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Mail not saved to disk: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async
    public void sendMailAsync(String[] to, String subject, String template, List<String> cc, Map<String, Object> model, File attachment) {
        sendMail(to, subject, template, cc, model, attachment);
    }
}
