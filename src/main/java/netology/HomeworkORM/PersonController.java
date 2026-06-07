package netology.HomeworkORM;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/persons")
public class PersonController {

    private final PersonDao personDao;

    // Инъекция зависимости через конструктор (Best Practice в Spring)
    public PersonController(PersonDao personDao) {
        this.personDao = personDao;
    }

    @GetMapping("/by-city")
    public List<Person> getPersonsByCity(@RequestParam String city) {
        return personDao.getPersonsByCity(city);
    }
}