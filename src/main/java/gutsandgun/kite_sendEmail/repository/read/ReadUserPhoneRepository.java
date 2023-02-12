package gutsandgun.kite_sendEmail.repository.read;

import gutsandgun.kite_sendEmail.entity.read.UserPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserPhoneRepository extends JpaRepository<UserPhone, Long> {
}
