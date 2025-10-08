package ru.aston.UserServiceAPI.dtos;

import jakarta.validation.constraints.*;

public class UserDTOIn {

    @NotNull
    @NotBlank
    @NotEmpty
    private String name;

    @Email
    private String email;

    @Min (18)
    @Max (99)
    private int age;

    public UserDTOIn() {
    }

    public UserDTOIn(String name,String email,int age) {
        this.name = name;
        this.email = email;
        this.age = age;
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
}
