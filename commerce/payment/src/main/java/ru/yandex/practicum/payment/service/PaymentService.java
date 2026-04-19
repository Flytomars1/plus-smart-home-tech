package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.payment.exception.NoOrderFoundException;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.model.PaymentStatus;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    private static final double VAT_RATE = 0.1;

    public Double productCost(OrderDto order) {
        log.info("Calculating product cost for order: {}", order.getOrderId());

        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Нет информации о товарах в заказе");
        }

        double total = 0.0;

        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId);
            total += product.getPrice().doubleValue() * quantity;
        }

        log.info("Total product cost for order {}: {}", order.getOrderId(), total);
        return total;
    }

    public Double getTotalCost(OrderDto order) {
        log.info("Calculating total cost for order: {}", order.getOrderId());

        Double productTotal = productCost(order);

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не рассчитана стоимость доставки");
        }
        Double deliveryTotal = order.getDeliveryPrice();

        Double vat = productTotal * VAT_RATE;
        Double total = productTotal + vat + deliveryTotal;

        log.info("Total cost: products={}, vat={}, delivery={}, total={}",
                productTotal, vat, deliveryTotal, total);

        return total;
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("Creating payment for order: {}", order.getOrderId());

        Double productTotal = productCost(order);
        Double vat = productTotal * VAT_RATE;

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не рассчитана стоимость доставки");
        }
        Double deliveryTotal = order.getDeliveryPrice();
        Double totalPayment = productTotal + vat + deliveryTotal;

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .productTotal(productTotal)
                .deliveryTotal(deliveryTotal)
                .feeTotal(vat)
                .totalPayment(totalPayment)
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(totalPayment)
                .deliveryTotal(deliveryTotal)
                .feeTotal(vat)
                .build();
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж не найден: " + paymentId));

        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        orderClient.paymentSuccess(payment.getOrderId());
        log.info("Payment {} marked as SUCCESS", paymentId);
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж не найден: " + paymentId));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        orderClient.paymentFailed(payment.getOrderId());
        log.info("Payment {} marked as FAILED", paymentId);
    }
}