package com.team.weread.service.impl;

import com.team.weread.dto.ChangePasswordRequest;
import com.team.weread.dto.LoginRequest;
import com.team.weread.model.User;
import com.team.weread.repository.UserRepository;
import com.team.weread.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User login(LoginRequest loginRequest) {
        if (loginRequest.getAccount().contains("@")) {
            User user = userRepository.findByEmail(loginRequest.getAccount());
            if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return user;
            }
        } else {
            User user = userRepository.findByUsername(loginRequest.getAccount());
            if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User changePassword(ChangePasswordRequest request) {
        User user;
        if (request.getAccount().contains("@")) {
            user = userRepository.findByEmail(request.getAccount());
        } else {
            user = userRepository.findByUsername(request.getAccount());
        }

        if (user != null && passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public Page<User> findUsers(int page, int size, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            Specification<User> specification = (root, query, criteriaBuilder) -> {
                String pattern = "%" + keyword.trim() + "%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(root.get("username"), pattern),
                        criteriaBuilder.like(root.get("nickname"), pattern),
                        criteriaBuilder.like(root.get("email"), pattern)
                );
            };
            return userRepository.findAll(specification, PageRequest.of(page, size));
        }
        return userRepository.findAll(PageRequest.of(page, size));
    }
}
