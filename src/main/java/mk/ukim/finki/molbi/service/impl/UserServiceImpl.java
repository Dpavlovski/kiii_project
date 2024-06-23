package mk.ukim.finki.molbi.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.enums.UserRole;
import mk.ukim.finki.molbi.repository.UserRepository;
import mk.ukim.finki.molbi.service.inter.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public String getUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse(null);
        }
        return null;
    }

    @Override
    public List<User> findUsersByRole(UserRole role) {
        return repository.findByRole(role);
    }
}
