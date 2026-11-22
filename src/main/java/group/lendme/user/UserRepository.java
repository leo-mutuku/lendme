package group.lendme.user;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(PhoneNumber phoneNumber);

    Optional<User> findByEmail(Email email);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);

    boolean existsByEmail(Email email);
}