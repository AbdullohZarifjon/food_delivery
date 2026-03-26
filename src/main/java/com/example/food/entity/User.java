package com.example.food.entity;

import com.example.food.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"password"})
@Table(name = "users",
        indexes = {
                @Index(name = "idx_phone", columnList = "phoneNumber"),
                @Index(name = "idx_username", columnList = "username")
        })

public class User extends BaseEntity implements UserDetails, Serializable {

    private String name;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus userStatus = UserStatus.ACTIVE;

    @Column(nullable = true)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    public User(String name, String phoneNumber, String username) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles; // Role GrantedAuthority'ni implement qilgani uchun bu yetarli
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userStatus != UserStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userStatus == UserStatus.ACTIVE;
    }
}
