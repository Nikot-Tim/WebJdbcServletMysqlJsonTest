package service;

import dao.UserDaoImpl;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Data
@Getter
@Setter
@Service
public class UserService {

    @Autowired
    private UserDaoImpl userDaoImpl;

    public UserDaoImpl getUserDaoImpl() {
        return userDaoImpl;
    }

    public void setUserDaoImpl(UserDaoImpl userDaoImpl) {
        this.userDaoImpl = userDaoImpl;
    }

    public boolean addUser(User user) throws IOException, SQLException {
        return userDaoImpl.saveUser(user);
    }

    public User findUser(int id) throws IOException, SQLException {
        return userDaoImpl.findUserById(id);
    }

    public List<User> findAllUsers() throws IOException, SQLException {
        return userDaoImpl.findAll();
    }
    public boolean deleteUser(int id) throws IOException, SQLException {
        return userDaoImpl.deleteUser(id);
    }
    public boolean updateUser(User user) throws IOException, SQLException {
        return userDaoImpl.updateUser(user);
    }
}
