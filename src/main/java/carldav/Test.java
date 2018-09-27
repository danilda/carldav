package carldav;

import carldav.entity.User;
import carldav.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Transactional
@Component
public class Test {

    private final UserRepository userRepository;

    @Autowired
    public Test(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    void init() {
//        User root = userRepository.findByEmailIgnoreCase("root");
        User test = userRepository.findByEmailIgnoreCase("test");
//        User test1 = userRepository.findByEmailIgnoreCase("test1");
//        test.getManagers().add(test1);
//        userRepository.save(test);

        System.out.println(test);
    }
}
