package com.example.food.mapper;


import com.example.food.dto.AdminDto;
import com.example.food.dto.CourierDto;
import com.example.food.entity.Courier;
import com.example.food.entity.Role;
import com.example.food.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(source = "userStatus", target = "status") // Nomlar har xil bo'lsa source/target shart
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    AdminDto.AdminResponse toAdminResponse(User user);

    default List<String> mapRoles(List<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getRole().name()) // Enumdan Stringga o'girish
                .toList();
    }

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.phoneNumber", target = "phoneNumber")
    @Mapping(source = "user.userStatus", target = "status")
    @Mapping(source = "courierType", target = "courierType")
    @Mapping(source = "vehicleNumber", target = "vehicleNumber")
    @Mapping(source = "carModel", target = "carModel")
    @Mapping(target = "roles", expression = "java(courier.getUser().getRoles().stream().map(r -> r.getRole().toString()).toList())")
    CourierDto.CourierResponse toCourierResponse(Courier courier);
}
