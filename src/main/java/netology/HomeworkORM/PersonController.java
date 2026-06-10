package netology.HomeworkORM.controller;

import netology.HomeworkORM.entity.Person;
import netology.HomeworkORM.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/persons")
public class PersonController {

    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/by-city")
    public List<Person> getPersonsByCity(@RequestParam String city) {
        return personRepository.findByCityOfLiving(city);
    }

    @GetMapping("/younger-than")
    public List<Person> getPersonsYoungerThan(@RequestParam Integer age) {
        return personRepository.findByAgeLessThanOrderByAgeAsc(age);
    }

    @GetMapping("/by-name-surname")
    public ResponseEntity<Person> getPersonByNameAndSurname(
            @RequestParam String name,
            @RequestParam String surname) {
        
        return personRepository.findByNameAndSurname(name, surname)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
