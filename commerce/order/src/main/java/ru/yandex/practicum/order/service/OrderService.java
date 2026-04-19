package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.DeliveryClient;
import ru.yandex.practicum.feign.PaymentClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.order.exception.NoOrderFoundException;
import ru.yandex.practicum.order.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.order.exception.NotAuthorizedUserException;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    public List<OrderDto> getClientOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }

        return orderRepository.findAll().stream()
                .map(this::toOrderDto)
                .toList();
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Creating new order from shopping cart: {}", request.getShoppingCart().getShoppingCartId());

        BookedProductsDto bookedProducts;
        try {
            bookedProducts = warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());
        } catch (Exception e) {
            throw new NoSpecifiedProductInWarehouseException("Нет заказываемого товара на складе: " + e.getMessage());
        }

        Order order = Order.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(bookedProducts.getDeliveryWeight())
                .deliveryVolume(bookedProducts.getDeliveryVolume())
                .fragile(bookedProducts.getFragile())
                .build();

        order = orderRepository.save(order);
        log.info("Order created with id: {}", order.getOrderId());

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId, AddressDto deliveryAddress) {
        log.info("Calculating delivery cost for order: {}", orderId);

        Order order = getOrderById(orderId);
        OrderDto orderDto = toOrderDto(order);

        DeliveryCostRequest request = DeliveryCostRequest.builder()
                .order(orderDto)
                .deliveryAddress(deliveryAddress)
                .build();

        Double deliveryPrice = deliveryClient.deliveryCost(request);
        order.setDeliveryPrice(deliveryPrice);
        order = orderRepository.save(order);

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Calculating total cost for order: {}", orderId);

        Order order = getOrderById(orderId);
        OrderDto orderDto = toOrderDto(order);

        Double totalPrice = paymentClient.getTotalCost(orderDto);
        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("Processing payment for order: {}", orderId);

        Order order = getOrderById(orderId);

        if (order.getState() != OrderState.NEW && order.getState() != OrderState.ON_PAYMENT) {
            throw new IllegalStateException("Заказ не может быть оплачен в текущем статусе: " + order.getState());
        }

        OrderDto orderDto = toOrderDto(order);
        PaymentDto paymentDto = paymentClient.payment(orderDto);

        order.setPaymentId(paymentDto.getPaymentId());
        order.setState(OrderState.PAID);
        order.setTotalPrice(paymentDto.getTotalPayment());

        order = orderRepository.save(order);
        log.info("Payment completed for order: {}, paymentId: {}", orderId, paymentDto.getPaymentId());

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("Starting assembly for order: {}", orderId);

        Order order = getOrderById(orderId);

        if (order.getState() != OrderState.PAID) {
            throw new IllegalStateException("Заказ должен быть оплачен перед сборкой. Текущий статус: " + order.getState());
        }

        AssemblyProductsForOrderRequest request = AssemblyProductsForOrderRequest.builder()
                .orderId(order.getOrderId())
                .products(order.getProducts())
                .build();

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductsForOrder(request);

        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);

        log.info("Assembly completed for order: {}", orderId);

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto delivery(UUID orderId, AddressDto deliveryAddress) {
        log.info("Starting delivery for order: {}", orderId);

        Order order = getOrderById(orderId);

        if (order.getState() != OrderState.ASSEMBLED) {
            throw new IllegalStateException("Заказ должен быть собран перед доставкой. Текущий статус: " + order.getState());
        }

        OrderDto orderDto = toOrderDto(order);

        DeliveryDto deliveryDto = DeliveryDto.builder()
                .orderId(order.getOrderId())
                .toAddress(deliveryAddress)
                .build();

        DeliveryRequest request = DeliveryRequest.builder()
                .deliveryDto(deliveryDto)
                .order(orderDto)
                .build();

        DeliveryDto createdDelivery = deliveryClient.planDelivery(request);

        order.setDeliveryId(createdDelivery.getDeliveryId());
        order.setState(OrderState.ON_DELIVERY);
        order = orderRepository.save(order);

        log.info("Delivery planned for order: {}, deliveryId: {}", orderId, createdDelivery.getDeliveryId());

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        return toOrderDto(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        return toOrderDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        return toOrderDto(order);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        order = orderRepository.save(order);
        return toOrderDto(order);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = getOrderById(request.getOrderId());

        Map<UUID, Long> currentProducts = order.getProducts();
        request.getProducts().forEach((productId, quantity) -> {
            currentProducts.merge(productId, quantity, (old, returned) -> {
                long newQuantity = old - returned;
                if (newQuantity <= 0) {
                    currentProducts.remove(productId);
                } else {
                    return newQuantity;
                }
                return null;
            });
        });

        order.setProducts(currentProducts);
        order.setState(OrderState.PRODUCT_RETURNED);
        order = orderRepository.save(order);

        return toOrderDto(order);
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден: " + orderId));
    }

    private OrderDto toOrderDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .products(order.getProducts())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }
}