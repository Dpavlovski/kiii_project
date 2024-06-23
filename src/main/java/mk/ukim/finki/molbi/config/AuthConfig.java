package mk.ukim.finki.molbi.config;

import mk.ukim.finki.molbi.model.enums.AppRole;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;

public class AuthConfig {

    public HttpSecurity authorize(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/admin",
                                "/admin/add",
                                "/admin/edit/*",
                                "/admin/save",
                                "/admin/delete/*",
                                "/admin/updateStatus/*/*",
                                "/admin/process/*/*",
                                "/admin/LATE_COURSE_ENROLLMENT/*",
                                "/admin/GENERAL/*",
                                "/admin/COURSE_ENROLLMENT_WITHOUT_REQUIREMENTS/*",
                                "/admin/STUDY_PROGRAM_CHANGE/*",
                                "/admin/COURSE_GROUP_CHANGE/*"
                        )
                        .hasAnyRole(AppRole.DEAN.name(), AppRole.ACADEMIC_AFFAIR_VICE_DEAN.name(),
                                AppRole.FINANCES_VICE_DEAN.name(),
                                AppRole.SCIENCE_AND_COOPERATION_VICE_DEAN.name(), AppRole.STUDENT_ADMINISTRATION.name(),
                                AppRole.STUDENT_ADMINISTRATION_MANAGER.name(), AppRole.ADMINISTRATION_MANAGER.name())
                        .requestMatchers(
                                "/student",
                                "/student/my_requests",
                                "/student/details/*",
                                "/student/*/apply",
                                "/student/*/edit/*",
                                "/student/delete/*")
                        .hasAnyRole(AppRole.STUDENT.name())
                        .requestMatchers(
                                "/professor",
                                "/professor/professorApprove/*")
                        .hasAnyRole(AppRole.PROFESSOR.name())
                        .requestMatchers(// todo: review in #14
                                "/INSTALLMENT_PAYMENT/updateStatus/*",
                                "/PAYMENT_DISCOUNT_SINGLE_PARENT/updateStatus/*")
                        .hasAnyRole(AppRole.FINANCES_VICE_DEAN.name())
                        .requestMatchers("/", "io.png").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .logout(LogoutConfigurer::permitAll);
    }
}
