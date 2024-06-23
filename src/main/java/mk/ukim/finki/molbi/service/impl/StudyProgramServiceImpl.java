package mk.ukim.finki.molbi.service.impl;

import lombok.AllArgsConstructor;
import mk.ukim.finki.molbi.model.base.StudyProgram;
import mk.ukim.finki.molbi.repository.StudentRepository;
import mk.ukim.finki.molbi.repository.StudyProgramRepository;
import mk.ukim.finki.molbi.service.inter.StudyProgramService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StudyProgramServiceImpl implements StudyProgramService {
    private final StudyProgramRepository studyProgramRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<StudyProgram> listAll() {
        return studyProgramRepository.findAll();
    }
}
