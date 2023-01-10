package gutsandgun.kite_sendemail.repository.read;

import gutsandgun.kite_sendemail.entity.read.UserEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserEmailRepository extends JpaRepository<UserEmail, Long> {
}
