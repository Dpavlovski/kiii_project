package mk.ukim.finki.molbi.model.enums;

public enum AppRole {
    PROFESSOR, FINANCES_VICE_DEAN, ACADEMIC_AFFAIR_VICE_DEAN, STUDENT_ADMINISTRATION, STUDENT,
    SCIENCE_AND_COOPERATION_VICE_DEAN, DEAN, STUDENT_ADMINISTRATION_MANAGER, ADMINISTRATION_MANAGER, GUEST;


    public String roleName() {
        return "ROLE_" + this.name();
    }
}
