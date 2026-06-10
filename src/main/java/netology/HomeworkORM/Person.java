package netology.HomeworkORM;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "PERSONS")
public class Person {

    @EmbeddedId
    private PersonId id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "city_of_living")
    private String cityOfLiving;

    public Person() {}

    public Person(PersonId id, String phoneNumber, String cityOfLiving) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.cityOfLiving = cityOfLiving;
    }

    // Геттеры и сеттеры
    public PersonId getId() { return id; }
    public void setId(PersonId id) { this.id = id; }

    public String getName() { return id.getName(); } // Делегирование для удобства
    public String getSurname() { return id.getSurname(); }
    public Integer getAge() { return id.getAge(); }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCityOfLiving() { return cityOfLiving; }
    public void setCityOfLiving(String cityOfLiving) { this.cityOfLiving = cityOfLiving; }
}