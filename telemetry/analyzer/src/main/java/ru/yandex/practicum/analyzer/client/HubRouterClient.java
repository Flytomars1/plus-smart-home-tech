package ru.yandex.practicum.analyzer.client;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    public void sendAction(String hubId, String scenarioName, DeviceActionProto action) {
        try {
            long timestamp = System.currentTimeMillis();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(action)
                    .setTimestamp(timestamp)
                    .build();

            Empty response = hubRouterStub.handleDeviceAction(request);
            log.info("Команда отправлена в Hub Router: hubId={}, scenario={}, action={}",
                    hubId, scenarioName, action.getType());

        } catch (StatusRuntimeException e) {
            log.error("Ошибка при отправке команды в Hub Router: {}", e.getStatus(), e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при отправке команды", e);
        }
    }
}