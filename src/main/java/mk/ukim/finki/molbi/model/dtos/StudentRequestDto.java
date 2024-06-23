package mk.ukim.finki.molbi.model.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequestDto {

    private String student;

    private String description;

    private Long course;

    private String professor;

    private Integer instalmentsNum;

    private String currentProfessor;

    private String newProfessor;

    private String newStudyProgram;

    private String oldStudyProgram;
}


