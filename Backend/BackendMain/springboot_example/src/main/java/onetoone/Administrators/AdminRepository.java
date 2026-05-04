package onetoone.Administrators;

import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUserId(User userId);
    Admin findByUserId_UserId(Long id);
}
