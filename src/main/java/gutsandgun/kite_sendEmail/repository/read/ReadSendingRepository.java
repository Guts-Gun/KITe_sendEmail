package gutsandgun.kite_sendEmail.repository.read;

import gutsandgun.kite_sendEmail.entity.read.Sending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReadSendingRepository extends JpaRepository<Sending, Long> {
}
