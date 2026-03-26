package com.example.food.service.impl;

import com.example.food.dto.AuthDto;
import com.example.food.dto.request.RefreshTokenDto;
import com.example.food.entity.Role;
import com.example.food.entity.User;
import com.example.food.entity.enums.UserRole;
import com.example.food.entity.enums.UserStatus;
import com.example.food.exception.AccessDeniedException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.repo.UserRepository;
import com.example.food.security.JwtService;
import com.example.food.service.AuthService;
import com.example.food.service.RoleService;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceimpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceimpl(JwtService jwtService, UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.AuthRequestForCustomers authRequestForCustomers) {
        User user = getOrCreateUser(authRequestForCustomers.name(), authRequestForCustomers.phoneNumber());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthDto.AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDto.AuthResponse refreshToken(RefreshTokenDto refreshTokenDto) {
        String oldRefreshToken = refreshTokenDto.refreshToken();

        // 1. Tokenni tekshiramiz (Validatsiyadan o'tsa Claims qaytadi)
        Claims claims = jwtService.extractRefreshClaims(oldRefreshToken);

        // 1. Subject'dan Username'ni olamiz (chunki buildToken'da shunday berilgan)
        String username = claims.getSubject();

        // 2. Userni Username orqali topamiz
        User user = findByUsername(username);

        // 4. Yangi tokenlarni yaratamiz
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // 5. Ikkala yangi tokenni qaytaramiz
        return new AuthDto.AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String token, String s) {
        System.out.println("Logout");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDto.AuthResponse loginForAdmins(AuthDto.AuthRequestForAdminsAndCouriers loginDto) {
        return commonLogin(loginDto, UserRole.ROLE_ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDto.AuthResponse loginForCouriers(AuthDto.AuthRequestForAdminsAndCouriers loginDto) {
        return commonLogin(loginDto, UserRole.ROLE_COURIER);
    }

    public User getOrCreateUser(String name, String phoneNumber) {
        // 1. Ham telefon, ham ism bo'yicha qidiramiz
        return userRepository.findByPhoneNumberAndNameIgnoreCase(phoneNumber, name)
                .orElseGet(() -> {
                    // 2. Agar bu kombinatsiya topilmasa, demak bu:
                    // a) Yangi foydalanuvchi
                    // b) Raqamini o'zgartirmagan, lekin ismini o'zgartirgan eski foydalanuvchi
                    // c) Raqamning YANGI egasi (Ismi boshqa)

                    // BU YERDA: Agar raqam bazada bo'lsa-yu, ismi boshqa bo'lsa,
                    // biz yangi User ochishimiz kerak, eskini yangilash emas!
                    Role role = roleService.findRoleByName(UserRole.ROLE_USER);

                    User newUser = User.builder()
                            .name(name)
                            .phoneNumber(phoneNumber)
                            .username(phoneNumber + "_" + System.currentTimeMillis())
                            .build();

                    newUser.getRoles().add(role);
                    return userRepository.save(newUser);
                });
    }

    // Bitta umumiy metod
    private AuthDto.AuthResponse commonLogin(AuthDto.AuthRequestForAdminsAndCouriers loginDto, UserRole role) {
        User user = findByUsername(loginDto.username());
        checkRole(role, user);
        checkPassword(loginDto.password(), user);

        return new AuthDto.AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    private void checkPassword(String password, User user) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RecordNotFoundException("Username yoki parol noto'g'ri");
        }
    }

    private void checkRole(UserRole userRole, User user) {
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getRole().equals(userRole));

        if (!hasRole) {
            throw new AccessDeniedException("Foydalanuvchida kerakli ruxsatnomalar (" + userRole + ") mavjud emas!");
        }
    }

    private User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RecordNotFoundException("Foydalanuvchi topilmadi"));
    }


    private User findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RecordNotFoundException("Username yoki parol noto'g'ri"));

        if (user.getUserStatus() == UserStatus.BLOCKED) {
            throw new AccessDeniedException("Sizning hisobingiz bloklangan! Iltimos, administratorga murojaat qiling.");
        }

        return user;
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }
}
