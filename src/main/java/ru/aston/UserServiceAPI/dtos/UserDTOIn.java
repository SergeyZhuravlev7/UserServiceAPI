package ru.aston.UserServiceAPI.dtos;

import jakarta.validation.constraints.*;

public class UserDTOIn {

    @NotNull (message = "Name cant be a null.")
    @Pattern (regexp = "^[A-Z][a-z]{2,14}$", message = "Name should start with upper case letter and should be between 3 and 15 letters.")
    private String name;

    @Email (message = "Email should be a valid email.")
    @NotNull (message = "Email cant be a null.")
    @NotEmpty (message = "Email cant be empty.")
    private String email;

    @Min (value = 18, message = "Age should be equals or greater 18 years old.")
    @Max (value = 99, message = "Age should be smaller then 100 years old.")
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

    @Override
    public String toString() {
        return "UserDTOIn{" + "name='" + name + '\'' + ", email='" + email + '\'' + ", age=" + age + '}';
    }
}
