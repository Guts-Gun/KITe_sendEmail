package gutsandgun.kite_sendemail.repository.read;

import gutsandgun.kite_sendemail.entity.read.UserPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserPhoneRepository extends JpaRepository<UserPhone, Long> {
}
