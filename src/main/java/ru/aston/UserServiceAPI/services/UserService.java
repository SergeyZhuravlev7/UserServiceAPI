package ru.aston.UserServiceAPI.services;

import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> getUserById(Long id);

    List<UserDTOOut> getAllUsersWithPaginationAndSort(int page,int count,String sort);

    List<UserDTOOut> getAllUsersWithPagination(int page,int count);

    List<UserDTOOut> getAllUsersDefaultWithSort(String sort);

    List<UserDTOOut> getAllUsersDefault();

    Optional<User> getUserByName(String name);

    Optional<User> getUserByEmail(String email);

    User getUserFromDTO(UserDTOIn userDTOIn);

    UserDTOOut getDTOFromUser(User user);

    UserDTOOut createUser(UserDTOIn userDTOIn);

    Optional<UserDTOOut> deleteUserById(Long id);

    Optional<UserDTOOut> updateUser(Long id,UserDTOIn userDTOIn);

}
