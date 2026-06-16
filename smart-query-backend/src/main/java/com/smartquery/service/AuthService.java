package com.smartquery.service;

import com.smartquery.model.*;
import com.smartquery.repository.UserRepository;
import com.smartquery.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus("ACTIVE");

        Long userId = userRepository.save(user);
        String token = jwtUtil.generateToken(userId, user.getUsername());

        return new AuthResponse(token, "Bearer", userId, user.getUsername(), user.getNickname());
    }

    public AuthResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 临时：接受明文密码用于测试（实际项目中不要这样做！）
        boolean passwordMatch = request.getPassword().equals(user.getPassword()) || 
                               passwordEncoder.matches(request.getPassword(), user.getPassword());
        
        if (!passwordMatch) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("用户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), user.getNickname());
    }
}
