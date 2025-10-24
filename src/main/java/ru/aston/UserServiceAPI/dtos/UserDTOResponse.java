package ru.aston.UserServiceAPI.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties (ignoreUnknown = true)
public class UserDTOResponse {

    private Long id;

    private String name;

    private String email;

    private int age;

    public UserDTOResponse() {
    }

    public UserDTOResponse(String name,String email,int age) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserDTOResponse that = (UserDTOResponse) o;
        return age == that.age && Objects.equals(id,that.id) && Objects.equals(name,that.name) && Objects.equals(email,that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,name,email,age);
    }

    @Override
    public String toString() {
        return "UserDTOOut{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", age=" + age + '}';
    }
}
