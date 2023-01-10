package gutsandgun.kite_sendemail.repository.read;

import gutsandgun.kite_sendemail.entity.read.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserAddressRepository extends JpaRepository<UserAddress, Long> {
}
