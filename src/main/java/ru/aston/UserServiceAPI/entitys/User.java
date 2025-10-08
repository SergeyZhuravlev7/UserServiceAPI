package ru.aston.UserServiceAPI.entitys;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table (name = "users")
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false)
    private Long id;

    private String name;

    private String email;

    private int age;

    @DateTimeFormat (pattern = "dd.MM.yyyy HH:mm:ss:SS")
    private LocalDateTime created_at;

    @DateTimeFormat (pattern = "dd.MM.yyyy HH:mm:ss:SS")
    private LocalDateTime updated_at;

    public User() {
        this.created_at = LocalDateTime.now();
    }

    public User(String name,String email,int age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && Objects.equals(id,user.id) && Objects.equals(name,user.name) && Objects.equals(email,user.email) && Objects.equals(created_at,user.created_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,name,email,age,created_at);
    }

    public User updateUser(UserDTOIn userDTOIn) {
        this.setName(userDTOIn.getName());
        this.setEmail(userDTOIn.getEmail());
        this.setAge(userDTOIn.getAge());
        this.setUpdated_at(LocalDateTime.now());
        return this;
    }
}