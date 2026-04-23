package ru.yandex.practicum.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentStatus;
import ru.yandex.practicum.payment.model.Payment;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        if (payment == null) return null;

        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }

    public Payment toEntity(UUID orderId, BigDecimal productTotal, BigDecimal deliveryTotal, BigDecimal vat, BigDecimal totalPayment) {
        return Payment.builder()
                .orderId(orderId)
                .productTotal(productTotal)
                .deliveryTotal(deliveryTotal)
                .feeTotal(vat)
                .totalPayment(totalPayment)
                .status(PaymentStatus.PENDING)
                .build();
    }
}