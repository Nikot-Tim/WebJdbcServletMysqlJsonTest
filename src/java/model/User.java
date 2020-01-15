package model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class User {
    private int id;
    private String name;
    private String email;
    private String country;

    public User(){}

    public User(int id, String name, String email, String country) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.country = country;
    }
    public User(String name, String email, String country){
        this.name = name;
        this.email = email;
        this.country = country;
    }
}
