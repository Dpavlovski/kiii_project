package mk.ukim.finki.molbi.repository;

import mk.ukim.finki.molbi.model.base.User;
import mk.ukim.finki.molbi.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByRole(UserRole role);
}
