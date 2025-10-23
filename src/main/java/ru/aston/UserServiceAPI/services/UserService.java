package ru.aston.UserServiceAPI.services;

import ru.aston.UserServiceAPI.dtos.UserDTORequest;
import ru.aston.UserServiceAPI.dtos.UserDTOResponse;
import ru.aston.UserServiceAPI.entitys.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> getUserById(Long id);

    List<UserDTOResponse> getAllUsersWithPaginationAndSort(int page,int count,String sort);

    List<UserDTOResponse> getAllUsersWithPagination(int page,int count);

    List<UserDTOResponse> getAllUsersDefaultWithSort(String sort);

    List<UserDTOResponse> getAllUsersDefault();

    Optional<User> getUserByName(String name);

    Optional<User> getUserByEmail(String email);

    User getUserFromDTO(UserDTORequest userDTORequest);

    UserDTOResponse getDTOFromUser(User user);

    UserDTOResponse createUser(UserDTORequest userDTORequest);

    Optional<UserDTOResponse> deleteUserById(Long id);

    Optional<UserDTOResponse> updateUser(Long id,UserDTORequest userDTORequest);

}
