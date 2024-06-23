package mk.ukim.finki.molbi.service.scheduled;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.molbi.service.inter.*;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public class ScheduledEvent {

    private final LateCourseEnrollmentStudentRequestService lceService;
    private final GeneralStudentRequestService genService;
    private final CourseEnrollmentWithoutRequirementsStudentRequestService cwrService;
    private final CourseGroupChangeStudentRequestService cgcService;
    private final ChangeStudyProgramStudentRequestService cspService;


    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleEvents() {
        try {
            lceService.sendMailsToProfessors();
            genService.sendMailsToAAViceDean();
            cgcService.sendMailsToAAViceDean();
            cspService.sendMailsToAAViceDean();
            cwrService.sendMailsToAAViceDean();
        } catch (MessagingException e) {
            System.err.println("Error occurred while sending emails: " + e.getMessage());
        }
    }

}
