package com.example.food.entity;

import com.example.food.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@Table(name = "roles")
public class Role extends BaseEntity implements GrantedAuthority, Serializable {

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Override
    public String getAuthority() {
        return role.name();
    }

    public Role(UserRole role) {
        this.role = role;
    }
}