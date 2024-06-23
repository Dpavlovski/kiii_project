package mk.ukim.finki.molbi.model.enums;

import lombok.Getter;

public enum UserRole {
    STUDENT(false, true, AppRole.STUDENT),
    // professors
    PROFESSOR(true, false, AppRole.PROFESSOR),
    ACADEMIC_AFFAIR_VICE_DEAN(true, false, AppRole.ACADEMIC_AFFAIR_VICE_DEAN),
    SCIENCE_AND_COOPERATION_VICE_DEAN(true, false, AppRole.PROFESSOR),
    FINANCES_VICE_DEAN(true, false, AppRole.FINANCES_VICE_DEAN),
    DEAN(true, false, AppRole.PROFESSOR),
    // staff
    STUDENT_ADMINISTRATION(false, false, AppRole.STUDENT_ADMINISTRATION),
    STUDENT_ADMINISTRATION_MANAGER(false, false, AppRole.STUDENT_ADMINISTRATION),
    FINANCE_ADMINISTRATION(false, false),
    FINANCE_ADMINISTRATION_MANAGER(false, false),
    LEGAL_ADMINISTRATION(false, false),
    ARCHIVE_ADMINISTRATION(false, false),
    ADMINISTRATION_MANAGER(false, false),
    // external professors
    EXTERNAL(true, false);

    private final Boolean professor;

    private final Boolean student;

    @Getter
    public AppRole applicationRole = AppRole.GUEST;

    UserRole(Boolean professor, Boolean student, AppRole role) {
        this.professor = professor;
        this.student = student;
        this.applicationRole = role;
    }

    UserRole(Boolean professor, Boolean student) {
        this.professor = professor;
        this.student = student;
    }

    public Boolean isProfessor() {
        return professor;
    }

    public Boolean isStudent() {
        return student;
    }

    public String roleName() {
        return "ROLE_" + this.name();
    }

}
