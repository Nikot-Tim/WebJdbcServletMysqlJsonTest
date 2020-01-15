package dao;

import model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    boolean saveUser(User user) throws SQLException, IOException;
    User findUserById(int id) throws SQLException, IOException;
    List<User> findAll() throws SQLException, IOException;
    boolean deleteUser(int id) throws IOException, SQLException;
    boolean updateUser(User user) throws IOException, SQLException;
}
