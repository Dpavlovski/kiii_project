package mk.ukim.finki.molbi.service.inter;

import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.enums.UserRole;

import java.util.List;

public interface UserService {
    String getUserId();

    String getUserRole();

    List<User> findUsersByRole(UserRole role);
}
