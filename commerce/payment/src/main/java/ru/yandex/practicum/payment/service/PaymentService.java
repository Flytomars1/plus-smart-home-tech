package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentStatus;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.payment.exception.NoOrderFoundException;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
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
    private final PaymentMapper paymentMapper;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.1");

    public BigDecimal productCost(OrderDto order) {
        log.info("Calculating product cost for order: {}", order.getOrderId());

        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Нет информации о товарах в заказе");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId);
            BigDecimal productPrice = product.getPrice();
            BigDecimal subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);
        }

        log.info("Total product cost for order {}: {}", order.getOrderId(), total);
        return total;
    }

    public BigDecimal getTotalCost(OrderDto order) {
        log.info("Calculating total cost for order: {}", order.getOrderId());

        BigDecimal productTotal = productCost(order);

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не рассчитана стоимость доставки");
        }
        BigDecimal deliveryTotal = order.getDeliveryPrice();

        BigDecimal vat = productTotal.multiply(VAT_RATE);
        BigDecimal total = productTotal.add(vat).add(deliveryTotal);

        log.info("Total cost: products={}, vat={}, delivery={}, total={}",
                productTotal, vat, deliveryTotal, total);

        return total;
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("Creating payment for order: {}", order.getOrderId());

        BigDecimal productTotal = productCost(order);
        BigDecimal vat = productTotal.multiply(VAT_RATE);

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не рассчитана стоимость доставки");
        }
        BigDecimal deliveryTotal = order.getDeliveryPrice();
        BigDecimal totalPayment = productTotal.add(vat).add(deliveryTotal);

        Payment payment = paymentMapper.toEntity(
                order.getOrderId(),
                productTotal,
                deliveryTotal,
                vat,
                totalPayment
        );

        payment = paymentRepository.save(payment);
        log.info("Payment created with id: {}, total: {}", payment.getPaymentId(), totalPayment);

        return paymentMapper.toDto(payment);
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