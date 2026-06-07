package netology.HomeworkORM;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PersonId implements Serializable {
    private String name;
    private String surname;
    private Integer age;

    public PersonId() {}

    public PersonId(String name, String surname, Integer age) {
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    // equals и hashCode ОБЯЗАТЕЛЬНЫ для составных ключей в Hibernate
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonId personId = (PersonId) o;
        return Objects.equals(name, personId.name) &&
                Objects.equals(surname, personId.surname) &&
                Objects.equals(age, personId.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, age);
    }
}