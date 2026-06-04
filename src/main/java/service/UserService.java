package service;

import model.User;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;
import service.dtos.UserDto;
import service.validator.UserValidator;

import java.time.LocalDate;
import java.util.List;

public class UserService {
    private UserRepository userRepository;
    private UserValidator userValidator;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userValidator = new  UserValidator();
    }

    public User addUser(String username, String email,
                        String password, LocalDate joinDate) {

        userValidator.validate(new UserDto(username, email, password, joinDate));

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        var user =  new User(username, email, hashedPassword, joinDate);
        return userRepository.add(user);
    }

    public User loginUser(String email, String password) {
        var user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        boolean matches = BCrypt.checkpw(password, user.getPassword());

        if (!matches) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public void updateUser(Integer id, String username, String email, String password, LocalDate joinDate) {
        userValidator.validate(new UserDto(username, email, password, joinDate));

        var user = new User(username, email, password, joinDate);
        user.setId(id);
        userRepository.update(id,  user);
    }

    public void deleteUser(int id) {
        userRepository.delete(id);
    }

    public User findOne(int id) {
        return userRepository.findOne(id);
    }

    public List<User> findAll() {
        return (List<User>)userRepository.findAll();
    }
}
