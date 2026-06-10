package netology.HomeworkORM;

import netology.HomeworkORM.Person;
import netology.HomeworkORM.PersonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, PersonId> {

    // 1. cityOfLiving - это прямое поле в Person, здесь Spring Data справится сам
    List<Person> findByCityOfLiving(String city);

    // 2. age находится внутри составного ключа (id). Пишем явный JPQL запрос.
    @Query("SELECT p FROM Person p WHERE p.id.age < :age ORDER BY p.id.age ASC")
    List<Person> findByAgeLessThanOrderByAgeAsc(@Param("age") Integer age);

    // 3. name и surname тоже находятся внутри составного ключа (id).
    @Query("SELECT p FROM Person p WHERE p.id.name = :name AND p.id.surname = :surname")
    Optional<Person> findByNameAndSurname(@Param("name") String name, @Param("surname") String surname);
}