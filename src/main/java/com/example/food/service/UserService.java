package com.example.food.service;

import com.example.food.dto.AdminDto;
import com.example.food.dto.CourierDto;
import com.example.food.entity.enums.UserStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    AdminDto.AdminResponse createAdmin(AdminDto.AdminRequest adminDto);

    CourierDto.CourierResponse createCourier(CourierDto.CourierRequest courierDto);

    AdminDto.AdminResponse getSelfProfile();

    Page<AdminDto.AdminResponse> getAllAdmins(Pageable pageable);

    void changeStatus(UUID id, UserStatus status);

    AdminDto.AdminResponse getUserById();

    AdminDto.AdminResponse updateProfile(@Valid AdminDto.AdminUpdateDto request);

    Object getUserFullDetails(UUID id);
}
