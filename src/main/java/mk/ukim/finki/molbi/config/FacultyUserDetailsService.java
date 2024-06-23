package mk.ukim.finki.molbi.config;

import mk.ukim.finki.molbi.model.base.Professor;
import mk.ukim.finki.molbi.model.base.Student;
import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.enums.UserRole;
import mk.ukim.finki.molbi.model.exceptions.InvalidUsernameException;
import mk.ukim.finki.molbi.repository.UserRepository;
import mk.ukim.finki.molbi.service.inter.ProfessorService;
import mk.ukim.finki.molbi.service.inter.StudentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FacultyUserDetailsService implements UserDetailsService {

    final UserRepository userRepository;
    final ProfessorService professorService;
    final StudentService studentService;
    final PasswordEncoder passwordEncoder;
    @Value("${system.authentication.password}")
    String systemAuthenticationPassword;

    public FacultyUserDetailsService(UserRepository userRepository, ProfessorService professorService,
                                     StudentService studentService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.professorService = professorService;
        this.studentService = studentService;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(username).orElse(null);
        if (user == null) {
            Student student = studentService.findByIndex(username);
            if (student == null) {
                throw new InvalidUsernameException();
            }
            user = new User();
            user.setId(username);
            user.setRole(UserRole.STUDENT);
            return new FacultyUserDetails(user, student, passwordEncoder.encode(systemAuthenticationPassword));
        }
        if (user.getRole().isProfessor()) {
            Professor professor = professorService.findById(username);
            return new FacultyUserDetails(user, professor, passwordEncoder.encode(systemAuthenticationPassword));
        }

        return new FacultyUserDetails(user, passwordEncoder.encode(systemAuthenticationPassword));
    }
}
