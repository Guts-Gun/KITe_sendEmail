package gutsandgun.kite_sendEmail.repository.read;

import gutsandgun.kite_sendEmail.entity.read.SendingBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadSendingBlockRepository extends JpaRepository<SendingBlock, Long> {
}
