package onetoone.Persons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Person findById(int id);

    @Transactional
    void deleteById(int id);

    Person findByLaptop_Id(int id);

    List<Person> findByNameContaining(String name);
    List<Person> findByIfActive(boolean active);
}
