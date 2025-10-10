package ru.aston.UserServiceAPI.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.UserServiceAPI.Utils.Updatable;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.repos.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PaginationService paginationService;

    private String defaultSort = "asc";
    private int defaultPage = 0;
    private int defaultSize = 10;


    @Autowired
    public UserServiceImpl(UserRepository userRepository,ObjectMapper objectMapper,PaginationService paginationService) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.paginationService = paginationService;
    }

    @Transactional (readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserDTOOut> getAllUsersWithPaginationAndSort(int page,int count,String sort) {
        return paginationService.getAllUsersWithPaginationAndSort(page,count,sort);
    }

    public List<UserDTOOut> getAllUsersWithPagination(int page,int count) {
        return getAllUsersWithPaginationAndSort(page,count,defaultSort);
    }

    public List<UserDTOOut> getAllUsersDefaultWithSort(String sort) {
        return getAllUsersWithPaginationAndSort(defaultPage,defaultSize,sort);
    }

    public List<UserDTOOut> getAllUsersDefault() {
        return getAllUsersWithPaginationAndSort(defaultPage,defaultSize,defaultSort);
    }

    @Transactional (readOnly = true)
    public Optional<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    @Transactional (readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserFromDTO(UserDTOIn userDTOIn) {
        return objectMapper.convertValue(userDTOIn,User.class);
    }

    public UserDTOOut getDTOFromUser(User user) {
        return objectMapper.convertValue(user,UserDTOOut.class);
    }

    @Transactional
    @Updatable
    public UserDTOOut createUser(UserDTOIn userDTOIn) {
        User user = userRepository.save(getUserFromDTO(userDTOIn));
        return objectMapper.convertValue(user,UserDTOOut.class);
    }

    @Transactional
    public Optional<UserDTOOut> deleteUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            userRepository.deleteById(id);
            return Optional.of(getDTOFromUser(userOptional.get()));
        }
        return Optional.empty();
    }

    @Transactional
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
