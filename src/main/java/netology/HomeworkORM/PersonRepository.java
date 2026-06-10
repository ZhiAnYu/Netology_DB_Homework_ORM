package netology.HomeworkORM.repository;

import netology.HomeworkORM.entity.Person;
import netology.HomeworkORM.entity.PersonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, PersonId> {

    // 1. Возвращает Entity по городу
    List<Person> findByCityOfLiving(String city);

    // 2. Возвращает Entity, возраст которых меньше переданного, отсортированные по возрастанию
    List<Person> findByAgeLessThanOrderByAgeAsc(Integer age);

    // 3. Возвращает Optional Entity по имени и фамилии
    Optional<Person> findByNameAndSurname(String name, String surname);
}
