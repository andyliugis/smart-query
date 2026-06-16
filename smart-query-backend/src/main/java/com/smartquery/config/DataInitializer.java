package com.smartquery.config;

import com.smartquery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// 暂时禁用，先让系统启动
// @Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 检查用户是否已存在
        if (userRepository.findByUsername("admin") == null) {
            com.smartquery.model.SysUser admin = new com.smartquery.model.SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setNickname("管理员");
            admin.setStatus("ACTIVE");
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("demo") == null) {
            com.smartquery.model.SysUser demo = new com.smartquery.model.SysUser();
            demo.setUsername("demo");
            demo.setPassword(passwordEncoder.encode("admin123"));
            demo.setEmail("demo@example.com");
            demo.setNickname("演示用户");
            demo.setStatus("ACTIVE");
            userRepository.save(demo);
        }
    }
}
