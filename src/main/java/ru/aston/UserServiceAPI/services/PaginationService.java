package ru.aston.UserServiceAPI.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.repos.UserRepository;

import java.util.List;

@Service
public class PaginationService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PaginationService(UserRepository userRepository,ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional (isolation = Isolation.SERIALIZABLE)
    public List<UserDTOOut> getAllUsersWithPaginationAndSort(int page,int count,String sort) {
        Page<User> userList;
        if (sort.equals("asc")) {
            userList = userRepository.findAll(PageRequest.of(page,count,Sort
                    .by("id")
                    .ascending()));
        } else userList = userRepository.findAll(PageRequest.of(page,count,Sort
                .by("id")
                .descending()));
        return userList
                .stream()
                .map(user -> objectMapper.convertValue(user,UserDTOOut.class))
                .toList();
    }
}
