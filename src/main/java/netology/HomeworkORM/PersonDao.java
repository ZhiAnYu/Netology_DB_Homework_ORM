package netology.HomeworkORM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Возвращает список пользователей, проживающих в указанном городе.
     */
    public List<Person> getPersonsByCity(String city) {
        // Используем JPQL (Java Persistence Query Language)
        String jpql = "SELECT p FROM Person p WHERE p.cityOfLiving = :city";

        return entityManager.createQuery(jpql, Person.class)
                .setParameter("city", city)
                .getResultList();
    }

    /**
     * Дополнительный метод из условия прошлой задачи: поиск людей старше 27 лет,
     * отсортированных по убыванию возраста.
     */
    public List<Person> getPersonsOlderThan(int age) {
        String jpql = "SELECT p FROM Person p WHERE p.id.age > :age ORDER BY p.id.age DESC";
        return entityManager.createQuery(jpql, Person.class)
                .setParameter("age", age)
                .getResultList();
    }

    // Метод для сохранения (для тестирования)
    public Person save(Person person) {
        entityManager.persist(person);
        return person;
    }
}