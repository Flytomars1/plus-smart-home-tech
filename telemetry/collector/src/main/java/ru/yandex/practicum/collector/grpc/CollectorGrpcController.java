package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.collector.handler.HubEventHandler;
import ru.yandex.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class CollectorGrpcController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubHandlers;

    @Autowired
    public CollectorGrpcController(
            Set<SensorEventHandler> sensorEventHandlers,
            Set<HubEventHandler> hubEventHandlers) {

        this.sensorHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity()
                ));

        this.hubHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));

        log.info("Загружено обработчиков: сенсоры - {}, хабы - {}",
                sensorHandlers.size(), hubHandlers.size());
    }

    @Override
    public void collectSensorEvent(
            SensorEventProto request,
            StreamObserver<Empty> responseObserver) {

        log.info("Получено gRPC событие от датчика: type={}, id={}, hubId={}",
                request.getPayloadCase(), request.getId(), request.getHubId());

        try {
            SensorEventHandler handler = sensorHandlers.get(request.getPayloadCase());

            if (handler == null) {
                throw new IllegalArgumentException(
                        "Не найден обработчик для события: " + request.getPayloadCase());
            }

            handler.handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC события датчика", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(
            HubEventProto request,
            StreamObserver<Empty> responseObserver) {

        log.info("Получено gRPC событие от хаба: type={}, hubId={}",
                request.getPayloadCase(), request.getHubId());

        try {
            HubEventHandler handler = hubHandlers.get(request.getPayloadCase());

            if (handler == null) {
                throw new IllegalArgumentException(
                        "Не найден обработчик для события: " + request.getPayloadCase());
            }

            handler.handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC события хаба", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        }
    }
}