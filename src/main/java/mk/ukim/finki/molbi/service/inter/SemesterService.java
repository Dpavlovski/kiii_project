package mk.ukim.finki.molbi.service.inter;

import mk.ukim.finki.molbi.model.base.Semester;

import java.util.List;

public interface SemesterService {
    List<Semester> findAll();

    Semester findById(String code);


}
