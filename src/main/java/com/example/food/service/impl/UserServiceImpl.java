package com.example.food.service.impl;

import com.example.food.dto.AdminDto;
import com.example.food.dto.CourierDto;
import com.example.food.dto.UserDto;
import com.example.food.entity.Courier;
import com.example.food.entity.Role;
import com.example.food.entity.User;
import com.example.food.entity.enums.UserRole;
import com.example.food.entity.enums.UserStatus;
import com.example.food.exception.BadRequestException;
import com.example.food.exception.RecordAlreadyException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.UserMapper;
import com.example.food.repo.UserRepository;
import com.example.food.service.CourierService;
import com.example.food.service.RoleService;
import com.example.food.service.UserService;
import com.example.food.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CourierService courierService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "username", "phoneNumber");

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder, UserMapper userMapper, CourierService courierService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.courierService = courierService;
    }

    @Override
    @Transactional
    public AdminDto.AdminResponse createAdmin(AdminDto.AdminRequest adminDto) {
        existUsername(adminDto.username());

        Role role = roleService.findRoleByName(UserRole.ROLE_ADMIN);
        User newUser = User.builder()
                .name(adminDto.name())
                .username(adminDto.username())
                .phoneNumber(adminDto.phoneNumber())
                .password(passwordEncoder.encode(adminDto.password()))
                .build();

        newUser.getRoles().add(role);

        User savedUser = userRepository.save(newUser);
        return userMapper.toAdminResponse(savedUser);
    }

    @Override
    @Transactional
    public CourierDto.CourierResponse createCourier(CourierDto.CourierRequest courierDto) {
        checkCourierTypeByNutNullOrIsBlank(courierDto);
        existUsername(courierDto.username());

        Role role = roleService.findRoleByName(UserRole.ROLE_COURIER);
        User newUser = User.builder()
                .name(courierDto.name())
                .username(courierDto.username())
                .phoneNumber(courierDto.phoneNumber())
                .password(passwordEncoder.encode(courierDto.password()))
                .build();

        newUser.getRoles().add(role);

        User savedUser = userRepository.save(newUser);

        Courier courier = courierService.createInitialInfo(savedUser, courierDto);

        return userMapper.toCourierResponse(courier);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDto.AdminResponse getSelfProfile() {
        User currentUser = SecurityUtils.getCurrentUser();

        // 3. Endi Mapper chaqirilganda rollar allaqachon ichida bo'ladi
        return userMapper.toAdminResponse(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminDto.AdminResponse> getAllAdmins(Pageable pageable) {
        List<Sort.Order> cleanOrders = new ArrayList<>();

        // 1. Sort parametrlarini massiv ko'rinishida bo'lsa ham qayta ishlash
        pageable.getSort().forEach(order -> {
            // Kelgan property'ni tozalaymiz: ["name", "id"] -> name, id
            String cleanedProperty = order.getProperty().replaceAll("[\\[\\]\"']", "");

            // Agar ichida vergul bo'lsa (ya'ni massiv kelgan bo'lsa), ularni bo'laklarga ajratamiz
            String[] properties = cleanedProperty.split(",");

            for (String prop : properties) {
                String trimmedProp = prop.trim(); // Bo'shliqlarni olib tashlaymiz

                if (!trimmedProp.isEmpty()) {
                    // Whitelist (ALLOWED_SORT_FIELDS) tekshiruvi
                    if (!ALLOWED_SORT_FIELDS.contains(trimmedProp)) {
                        throw new BadRequestException("Saralash uchun noto'g'ri maydon: " + trimmedProp);
                    }

                    // Har bir maydon uchun alohida Order obyektini qo'shamiz
                    cleanOrders.add(new Sort.Order(order.getDirection(), trimmedProp));
                }
            }
        });

        // 2. Agar sort bo'sh bo'lsa yoki noto'g'ri bo'lsa, default "name" bo'yicha tartiblaymiz
        if (cleanOrders.isEmpty()) {
            cleanOrders.add(Sort.Order.asc("name"));
        }

        // 3. Tozalangan va massivdan ajratilgan Sort bilan yangi Pageable yaratamiz
        Pageable cleanedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(cleanOrders)
        );

        // 4. Repository-ga tozalangan Pageable-ni beramiz
        return userRepository.findAllByRoles_Role(UserRole.ROLE_ADMIN, cleanedPageable)
                .map(userMapper::toAdminResponse);
    }

    @Override
    @Transactional
    public void changeStatus(UUID id, UserStatus status) {
        // 1. Foydalanuvchini bazadan qidiramiz
        User user = findUserById(id);

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        if (user.getId().equals(currentUserId)) {
            throw new BadRequestException("SuperAdmin o'z statusini o'zi o'zgartira olmaydi!");
        }

        // 3. Statusni yangilash
        user.setUserStatus(status);

        // 4. Saqlash
        userRepository.save(user);

        // TODO: Kelajakda agar user BLOCKED bo'lsa, uning barcha aktiv sessiyalarini Redisdan o'chirish kerak
//        log.info("User {} statusi {} ga o'zgartirildi. Bajardi: {}", id, status, currentUser.getUsername());
    }

    @Override
    public AdminDto.AdminResponse getUserById() {
        User currentUser = SecurityUtils.getCurrentUser();
        return userMapper.toAdminResponse(currentUser);
    }

    @Override
    @Transactional
    public AdminDto.AdminResponse updateProfile(AdminDto.AdminUpdateDto request) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (!currentUser.getPhoneNumber().equals(request.phoneNumber())) {
            existPhoneNumber(request.phoneNumber());
            currentUser.setPhoneNumber(request.phoneNumber());
        }

        currentUser.setName(request.name());
        // save() shart emas, chunki @Transactional dirty checking orqali o'zi saqlaydi
        return userMapper.toAdminResponse(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getUserFullDetails(UUID id) {
        User user = findUserById(id);

        Set<UserRole> roleNames = user.getRoles().stream()
                .map(Role::getRole)
                .collect(Collectors.toSet());

        if (roleNames.contains(UserRole.ROLE_COURIER)) {
            Courier courier = courierService.findById(user.getId());
            return userMapper.toCourierResponse(courier);
        }

        if (roleNames.contains(UserRole.ROLE_ADMIN)) {
            return userMapper.toAdminResponse(user);
        }

        throw new BadRequestException("Ushbu foydalanuvchi xodim emas!");
    }

    private void checkIdNaNotNull(UUID id) {
        if (id == null) {
            throw new BadRequestException("id is required");
        }
    }

    private void checkCourierTypeByNutNullOrIsBlank(CourierDto.CourierRequest courierDto) {
        if (courierDto.courierType() == null) {
            throw new BadRequestException("Kuriyer turi tanlanishi shart!");
        }
        if ("AUTO".equalsIgnoreCase(courierDto.courierType())) {
            if ((courierDto.vehicleNumber() == null || courierDto.vehicleNumber().isBlank())
                    || (courierDto.carModel() == null || courierDto.carModel().isBlank())) {
                throw new BadRequestException("Mashinali kurer uchun moshina raqami va modeli to'liq kiritilishi shart!");
            }
        } else if (!"WALK".equalsIgnoreCase(courierDto.courierType())) {
            throw new BadRequestException("Kuryer turi tanlanishi shart!");
        }
    }

    public void existUsername(String username) {
        if (userRepository.existsUserByUsername(username)) {
            throw new RecordAlreadyException("Bunday username mavjud!");
        }
    }

    private void existPhoneNumber(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RecordAlreadyException("Bu telefon raqami band!");
        }
    }

    public User findUserById(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RecordNotFoundException("User not found with id: " + id));
    }

}
