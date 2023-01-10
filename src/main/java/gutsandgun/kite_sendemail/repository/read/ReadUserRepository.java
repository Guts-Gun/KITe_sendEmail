package gutsandgun.kite_sendemail.repository.read;

import gutsandgun.kite_sendemail.entity.read.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReadUserRepository extends JpaRepository<User, Long> {
}
