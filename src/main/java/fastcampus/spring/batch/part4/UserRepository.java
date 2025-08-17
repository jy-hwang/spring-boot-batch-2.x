package fastcampus.spring.batch.part4;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Collection<UserEntity> findAllByUpdatedDate(LocalDate updatedDate);
}
