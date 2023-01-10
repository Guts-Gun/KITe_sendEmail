package gutsandgun.kite_sendemail.repository.read;

import gutsandgun.kite_sendemail.entity.read.AddressGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadAddressGroupRepository extends JpaRepository<AddressGroup, Long> {
}
