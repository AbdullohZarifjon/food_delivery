package com.example.food.entity.enums;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED;

    public boolean isUsable() {
        return this == ACTIVE;
    }
}
