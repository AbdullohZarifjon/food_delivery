package com.example.food.config;


import com.example.food.entity.Role;
import com.example.food.entity.User;
import com.example.food.entity.enums.UserRole;
import com.example.food.repo.RoleRepository;
import com.example.food.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DataLoader(RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Role> allRoles = roleRepository.findAll();
        Role role = new Role();
        Role role2 = new Role();
        Role role3 = new Role();
        Role role4 = new Role();

        if (allRoles.isEmpty()) {

            role.setRole(UserRole.ROLE_SUPERADMIN);
            roleRepository.save(role);

            role2.setRole(UserRole.ROLE_ADMIN);
            roleRepository.save(role2);

            role3.setRole(UserRole.ROLE_USER);
            roleRepository.save(role3);

            role4.setRole(UserRole.ROLE_COURIER);
            roleRepository.save(role4);
        }
        List<User> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            User user = User.builder()
                    .name("zarif")
                    .username("zarif")
                    .password(passwordEncoder.encode("abdullohzarif"))
                    .phoneNumber("+998902959199")
                    .build();

            user.getRoles().add(role);
            user.getRoles().add(role2);
            userRepository.save(user);

            User user2 = User.builder()
                    .name("admin")
                    .username("admin")
                    .password(passwordEncoder.encode("adminchik"))
                    .phoneNumber("+998902959199")
                    .build();

            user2.getRoles().add(role2);
            userRepository.save(user2);

        }
    }
}
