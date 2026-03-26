package com.example.food.controller;

import com.example.food.dto.AdminDto;
import com.example.food.dto.CourierDto;
import com.example.food.entity.enums.UserStatus;
import com.example.food.service.CourierService;
import com.example.food.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/superadmin") // Superadmin uchun maxsus namespace
@Tag(name = "Superadmin Management", description = "Faqat Superadmin uchun xodimlarni boshqarish APIlari")
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN')") // Butun Controller darajasiga qo'yamiz
public class SuperadminController {

    private final UserService userService;
    private final CourierService courierService;

    public SuperadminController(UserService userService, CourierService courierService) {
        this.userService = userService;
        this.courierService = courierService;
    }

    // --- CREATE ---
    @PostMapping("/admins")
    @Operation(summary = "Yangi Admin qo'shish")
    public ResponseEntity<AdminDto.AdminResponse> createAdmin(@RequestBody @Valid AdminDto.AdminRequest adminDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAdmin(adminDto));
    }


    @PostMapping("/couriers")
    @Operation(summary = "Yangi Kurer qo'shish")
    public ResponseEntity<CourierDto.CourierResponse> createCourier(@RequestBody @Valid CourierDto.CourierRequest courierDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createCourier(courierDto));
    }


    // --- READ (O'zi) ---
    @GetMapping("/me")
    @Operation(summary = "SuperAdmin: O'z ma'lumotlarini ko'rish")
    public ResponseEntity<AdminDto.AdminResponse> getAdmin() {
        return ResponseEntity.ok(userService.getSelfProfile());
    }


    @GetMapping("/users/{id}")
    @Operation(summary = "SuperAdmin: Xodim ma'lumotlarini ID orqali ko'rish")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
                    content = @Content(schema = @Schema(anyOf = {AdminDto.AdminResponse.class, CourierDto.CourierResponse.class})))
    })
    public ResponseEntity<Object> getUserById(@PathVariable UUID id) {
        // Bu yerda xodim admin yoki kurerligiga qarab har xil DTO qaytishi mumkin
        return ResponseEntity.ok(userService.getUserFullDetails(id));
    }


    // --- READ (Ro'yxat) ---
    @GetMapping("/admins")
    @Operation(summary = "Barcha Adminlar ro'yxati (Pagination sort: id, name, username, phoneNumber) orqalik olish")
    public ResponseEntity<Page<AdminDto.AdminResponse>> getAllAdmins(
            @PageableDefault(size = 10, page = 0, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllAdmins(pageable));
    }


    @GetMapping("/couriers")
    @Operation(summary = "Barcha Kurerlar ro'yxati (Paginationda sort faqat name orqalik qaytaradi asc)")
    public ResponseEntity<Page<CourierDto.CourierResponse>> getAllCouriers(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(courierService.getAllCouriers(pageable));
    }


    // --- UPDATE (Status) ---
    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Xodimni bloklash yoki aktivlashtirish")
    public ResponseEntity<Void> changeUserStatus(@PathVariable UUID id, @RequestParam UserStatus status) {
        userService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/me")
    @Operation(summary = "SuperAdmin: O'z ma'lumotlarini tahrirlash")
    public ResponseEntity<AdminDto.AdminResponse> updateSelf(@RequestBody @Valid AdminDto.AdminUpdateDto request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

}
