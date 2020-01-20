package web;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import model.User;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.stereotype.Component;
import service.UserService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.SQLException;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@Component
@WebServlet("/")
public class UserServlet extends HttpServlet {
    @Autowired
    private UserService userService = (UserService) new BeanFactory().getBean("userService");

    static {
        BeanFactory beanFactory = new BeanFactory();
        try {
            beanFactory.instantiate();

        } catch (URISyntaxException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final long serialVersionUID = 1;

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        doGet(req, resp);
    }
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        String path = req.getServletPath();

        try {
            switch (path){
                case ("/add"):
                    addUser(req, resp);
                    break;
                case ("/delete"):
                    deleteUser(req, resp);
                    break;
                case ("/edit"):
                    editUser(req, resp);
                    break;
                default:
                    listUser(req, resp);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void listUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        writer.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userService.findAllUsers()));
    }

    private void editUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        int id = Integer.parseInt(req.getParameter("id"));
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String country = req.getParameter("country");
        boolean success = userService.updateUser(new User(id, name, email, country));
        String operation = "User updated";
        returnJson(resp,operation,success);
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        int id = Integer.parseInt(req.getParameter("id"));
        boolean success = userService.deleteUser(id);
        String operation = "User deleted";
        returnJson(resp,operation,success);
    }

    private void addUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String country = req.getParameter("country");
        User user = new User(name, email, country);
        boolean success = userService.addUser(user);
        String operation = "New user is created";
        returnJson(resp,operation,success);
    }

    private void returnJson(HttpServletResponse resp, String operation, boolean success) throws IOException, SQLException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("users", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userService.findAllUsers()));
        jsonNode.put(operation, success);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.println(jsonNode);
    }
}
