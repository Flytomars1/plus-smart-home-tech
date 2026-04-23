package ru.yandex.practicum.payment.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.PaymentStatus;  // ← импорт из DTO

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    private UUID orderId;

    private BigDecimal productTotal;

    private BigDecimal deliveryTotal;

    private BigDecimal feeTotal;

    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}