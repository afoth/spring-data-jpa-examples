package com.afoth.examples.springjpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MapKey;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Optional;

@SpringBootApplication
public class SpringjpaApplication {

    public static void main(String[] args) {
		SpringApplication.run(SpringjpaApplication.class, args);
	}
}

@Entity
@Table(name = "parent")
@Data
@NoArgsConstructor
@AllArgsConstructor
class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(mappedBy = "parent", cascade = { CascadeType.ALL})
    FirstChild firstChild;

    @OneToOne(mappedBy = "parent", cascade = { CascadeType.ALL})
    SecondChild secondChild;
}

@Entity
@Data
@DiscriminatorValue("FirstChild")
class FirstChild extends Child{
}

@Entity
@Data
@DiscriminatorValue("SecondChild")
class SecondChild extends Child{

}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Child")
class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @MapKey
    Parent parent;
}

@Repository
interface ParentRepository extends JpaRepository<Parent, Long> {}

@Repository
interface ChildRepository extends JpaRepository<Child, Long> {}

@Service
class ParentService {
    @Autowired
    ParentRepository parentRepository;

    public Parent save(Parent parent) {
        Optional.ofNullable(parent.firstChild).ifPresent(c -> c.parent = parent);
        Optional.ofNullable(parent.secondChild).ifPresent(c -> c.parent = parent);
        return parentRepository.save(parent);
    }
}

@RestController
class ParentController {
    @RequestMapping("/")
    String index() {
        return "";
    }
}


@Component
class Inititalizer {

    @Autowired
    ParentService parentService;

    @PostConstruct
    public void init(){
        Parent parent = new Parent();
        parent.firstChild = new FirstChild();
        parent.secondChild = new SecondChild();
        parentService.save(parent);
    }
}

