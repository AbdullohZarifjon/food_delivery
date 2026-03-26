package com.example.food.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface PromotionDto {

    @ValidPromotion // Ichki annotatsiya
    record PromotionRequest(
            @NotBlank(message = "Aksiya nomi bo'sh bo'lmasligi shart")
            @Size(max = 100, message = "Aksiya nomi juda uzun")
            String title,

            @Min(value = 1, message = "Chegirma foizi kamida 1% bo'lishi shart")
            @Max(value = 100, message = "Chegirma foizi 100% dan oshmasligi shart")
            Integer discountPercentage,

            @Positive(message = "Chegirma summasi musbat bo'lishi shart")
            BigDecimal fixedDiscountAmount,

            @NotNull(message = "Boshlanish vaqti shart")
            @FutureOrPresent(message = "Boshlanish vaqti o'tib ketgan bo'lmasligi shart")
            LocalDateTime startDate,

            @NotNull(message = "Tugash vaqti shart")
            @Future(message = "Tugash vaqti kelajakda bo'lishi shart")
            LocalDateTime endDate

    ) {}


    record PromotionResponse(
            UUID id,
            String title,
            Integer discountPercentage,
            BigDecimal fixedDiscountAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean active
    ) {}

    // --- MANA SHU YERDAN VALIDATSIYA MANTIQI BOSHLANADI ---

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PromotionValidator.class)
    @Documented
    @interface ValidPromotion {
        String message() default "Yo chegirma foizi, yo aniq summa kiritilishi shart (ikkalasi birga emas)";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    class PromotionValidator implements ConstraintValidator<ValidPromotion, PromotionRequest> {
        @Override
        public boolean isValid(PromotionRequest request, ConstraintValidatorContext context) {
            if (request == null) return true;

            // 1. "Yo foiz, yo summa" (XOR algoritmi)
            boolean hasPercentage = request.discountPercentage() != null;
            boolean hasFixedAmount = request.fixedDiscountAmount() != null;
            boolean xorCheck = hasPercentage ^ hasFixedAmount;

            if (!xorCheck) return false;

            // 2. Vaqtlar mantiqi: Start < End bo'lishi shart
            if (request.startDate() != null && request.endDate() != null) {
                if (request.startDate().isAfter(request.endDate())) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Boshlanish vaqti tugash vaqtidan keyin bo'lishi mumkin emas")
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;
        }
    }
}