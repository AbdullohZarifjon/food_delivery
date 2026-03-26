package com.example.food.service.impl;

import com.example.food.entity.Branch;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.repo.BranchRepository;
import com.example.food.service.BranchService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public Branch getMainBranch() {
        return branchRepository.findFirstByMainTrueAndActiveTrue()
                .orElseGet(() -> branchRepository.findFirstByActiveTrue()
                        .orElseThrow(() -> new RecordNotFoundException("Hech qanday faol restoran topilmadi!")));
    }

    @Override
    public BigDecimal calculateDeliveryFee(Double distanceInMeters) {
        if (distanceInMeters <= 1000) return BigDecimal.ZERO; // 1km gacha bepul
        if (distanceInMeters <= 4000) return new BigDecimal("15000"); // 1-4km
        return new BigDecimal("25000"); // 4km dan uzoq bo'lsa
    }
}