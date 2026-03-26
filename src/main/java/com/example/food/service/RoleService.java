package com.example.food.service;

import com.example.food.entity.Role;
import com.example.food.entity.enums.UserRole;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.repo.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findRoleByName(UserRole role) {
        return roleRepository.findByRole(role)
                .orElseThrow(() -> new RecordNotFoundException("The role does not exist"));
    }
}
