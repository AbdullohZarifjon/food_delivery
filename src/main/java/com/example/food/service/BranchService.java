package com.example.food.service;

import com.example.food.entity.Branch;

import java.math.BigDecimal;

public interface BranchService {
    Branch getMainBranch();

    BigDecimal calculateDeliveryFee(Double distanceInMeters);
}
