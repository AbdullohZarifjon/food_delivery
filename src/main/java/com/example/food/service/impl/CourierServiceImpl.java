package com.example.food.service.impl;

import com.example.food.dto.CourierDto;
import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.entity.Courier;
import com.example.food.entity.User;
import com.example.food.entity.enums.CourierStatus;
import com.example.food.entity.enums.CourierType;
import com.example.food.exception.BadRequestException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.UserMapper;
import com.example.food.repo.CourierRepository;
import com.example.food.service.CourierService;
import com.example.food.service.OrderService;
import com.example.food.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CourierServiceImpl implements CourierService {

    private final CourierRepository courierRepository;
    private final UserMapper userMapper;
    private final OrderService orderService;

    public CourierServiceImpl(CourierRepository courierRepository, UserMapper userMapper, OrderService orderService) {
        this.courierRepository = courierRepository;
        this.userMapper = userMapper;
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public Courier createInitialInfo(User user, CourierDto.CourierRequest dto) {
        Courier courier = Courier.builder()
                .user(user)
                .courierType(CourierType.WALK)
                .status(CourierStatus.OFFLINE)
                .build();

        if ("AUTO".equalsIgnoreCase(dto.courierType())) {
            courier.setCourierType(CourierType.AUTO);
            courier.setVehicleNumber(dto.vehicleNumber());
            courier.setCarModel(dto.carModel());
        }
        return courierRepository.save(courier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourierDto.CourierResponse> getAllCouriers(Pageable pageable) {
        Pageable manualSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("user.name").ascending()
        );

        Page<Courier> courierPage = courierRepository.findAllActiveWithUser(manualSort);

        return courierPage.map(userMapper::toCourierResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Courier findById(UUID userId) {
        return courierRepository.findById(userId)
                .orElseThrow(() -> new RecordNotFoundException("Kurer ma'lumotlari topilmadi"));
    }

    @Override
    @Transactional
    public void updateStatus(boolean active) {
        // 1. JWT orqali joriy kuryer ID-sini olamiz
        // SecurityUtils - bu bizning yordamchi klassimiz bo'ladi
        UUID courierId = SecurityUtils.getCurrentUserId();

        Courier courier = findById(courierId);

        // 2. Biznes mantiq: Agar kuryer band bo'lsa (buyurtma ustida ishlayotgan bo'lsa),
        // u ishni to'xtata (OFFLINE bo'la) olmaydi.
        if (!active && courier.getStatus() == CourierStatus.BUSY) {
            throw new BadRequestException("Sizda faol buyurtma bor. Avval uni tugating!");
        }

        if (active) {
            courier.setStatus(CourierStatus.ONLINE);
        } else {
            courier.setStatus(CourierStatus.OFFLINE);
        }

        courierRepository.save(courier);
    }

    @Transactional
    @Override
    public void startDelivery() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        orderService.markOrdersAsOnTheWay(currentUserId);

        // 3. Kuryerni band qilish
        Courier courier = findById(currentUserId);
        courier.setStatus(CourierStatus.BUSY);
        courierRepository.save(courier);
    }

    @Override
    @Transactional
    public OrderDto.OrderResponseForMonitor acceptOrder(UUID id) {
        // 3. Joriy kurer ma'lumotlarini olish
        User currentUserCourier = SecurityUtils.getCurrentUser();

        Courier courier = findById(currentUserCourier.getId());

        if (courier.getStatus() == CourierStatus.OFFLINE) {
            throw new BadRequestException("Courier offline!");
        }

        return orderService.acceptOrderByCourier(id, courier, currentUserCourier);
    }

    @Override
    @Transactional
    public void completeCourierTask(UUID orderId, LocationDto.CourierLocationRequest location) {
        // 1. Kuryerni aniqlaymiz
        UUID courierId = SecurityUtils.getCurrentUserId();
        Courier courier = findById(courierId);

        // 2. Buyurtmani yopish (OrderService mantiqiy tekshiruvlarni qiladi)
        // Bu yerda Geofencing va Status o'zgaradi
        orderService.processOrderCompletion(orderId, location, courierId);

        // 3. Kuryerga tegishli ishlarni yakunlaymiz
        courier.setStatus(CourierStatus.ONLINE);
        courierRepository.save(courier);

    }

//    @Override
//    public void handleLocationUpdate(LocationDto.CourierLocationRequest locationDto) {
//        // Bizga kuryer kimligi kerak
//        UUID courierId = SecurityUtils.getCurrentUserId();
//
//        // Asosiy mantiqni OrderService'ga delegatsiya qilamiz
//        orderService.broadcastCourierLocation(courierId, locationDto);
//    }

}
