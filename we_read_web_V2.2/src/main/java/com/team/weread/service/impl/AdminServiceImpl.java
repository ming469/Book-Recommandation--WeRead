package com.team.weread.service.impl;

import com.team.weread.dto.LoginRequest;
import com.team.weread.model.Admin;
import com.team.weread.repository.AdminRepository;
import com.team.weread.service.AdminService;
import com.team.weread.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(AdminRepository adminRepository, BCryptPasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    @Override
    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    @Override
    public Admin login(LoginRequest loginRequest) {
        if (loginRequest.getAccount().contains("@")) {
            Admin admin = adminRepository.findByEmail(loginRequest.getAccount());
            if (admin != null && passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
                return admin;
            }
        } else {
            Admin admin = adminRepository.findByUsername(loginRequest.getAccount());
            if (admin != null && passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
                return admin;
            }
        }
        return null;
    }

    @Override
    public Admin updateAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    @Override
    public Admin findById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    @Override
    public boolean validateAdminToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String username = JwtUtil.extractUsername(token);
            if (username != null) {
                Admin admin = findByUsername(username);
                return admin != null && admin.getStatusId() == 1;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
