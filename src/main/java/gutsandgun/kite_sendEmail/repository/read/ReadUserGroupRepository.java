package gutsandgun.kite_sendEmail.repository.read;

import gutsandgun.kite_sendEmail.entity.read.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserGroupRepository extends JpaRepository<UserGroup, Long> {
}
