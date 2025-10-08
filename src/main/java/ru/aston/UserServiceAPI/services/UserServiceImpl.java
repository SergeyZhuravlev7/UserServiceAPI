package ru.aston.UserServiceAPI.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.repos.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional (readOnly = true,
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private String defaultSort = "asc";
    private int defaultPage = 0;
    private int defaultSize = 10;


    @Autowired
    public UserServiceImpl(UserRepository userRepository,ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional (isolation = Isolation.SERIALIZABLE)
    public List<UserDTOOut> getAllUsersWithPaginationAndSort(int page,int count,String sort) {
        Page<User> userList;
        if (sort.equals("asc")) {
            userList = userRepository.findAll(PageRequest.of(page,count,Sort.by("id").ascending()));
        } else userList = userRepository.findAll(PageRequest.of(page,count,Sort.by("id").descending()));
        return userList.stream().map(this::getDTOFromUser).toList();
    }

    @Transactional (propagation = Propagation.NEVER)
    public List<UserDTOOut> getAllUsersWithPagination(int page,int count) {
        return getAllUsersWithPaginationAndSort(page,count,defaultSort);
    }

    @Transactional (propagation = Propagation.NEVER)
    public List<UserDTOOut> getAllUsersDefaultWithSort(String sort) {
        return getAllUsersWithPaginationAndSort(defaultPage,defaultSize,sort);
    }

    @Transactional (propagation = Propagation.NEVER)
    public List<UserDTOOut> getAllUsersDefault() {
        return getAllUsersWithPaginationAndSort(defaultPage,defaultSize,defaultSort);
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserFromDTO(UserDTOIn userDTOIn) {
        return objectMapper.convertValue(userDTOIn,User.class);
    }

    public UserDTOOut getDTOFromUser(User user) {
        return objectMapper.convertValue(user,UserDTOOut.class);
    }

    @Transactional (readOnly = false)
    public UserDTOOut createUser(UserDTOIn userDTOIn) {
        User user = userRepository.save(getUserFromDTO(userDTOIn));
        return objectMapper.convertValue(user,UserDTOOut.class);
    }

    @Transactional (readOnly = false)
    public Optional<UserDTOOut> deleteUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            userRepository.deleteById(id);
            return Optional.of(getDTOFromUser(userOptional.get()));
        }
        return Optional.empty();
    }

    @Transactional (readOnly = false)
    public Optional<UserDTOOut> updateUser(Long id,UserDTOIn userDTOIn) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            User updatedUser = user.updateUser(userDTOIn);
            userRepository.save(updatedUser);
            return Optional.of(getDTOFromUser(updatedUser));
        }
        return Optional.empty();
    }

    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }

    public int getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public int getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }
}
