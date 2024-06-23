package mk.ukim.finki.molbi.service.inter;

import mk.ukim.finki.molbi.model.base.Subject;

import java.util.List;

public interface SubjectService {
    List<Subject> listAll();

    Subject findById(String id);
}
