package org.ftc.server;

import org.ftc.Ftc;
import org.ftc.server.engine.Engine;

import java.util.UUID;

import io.grpc.stub.StreamObserver;
import lombok.Data;

@Data
public class ClientHandler implements StreamObserver<Ftc.UUIDAndPosition> {

    private final Engine engine;
    private final StreamObserver<Ftc.RiskEstimation> responseObserver;

    @Override
    public void onNext(Ftc.UUIDAndPosition uuidAndPosition) {
        final UUID clientUUID = UUID.fromString(uuidAndPosition.getUUID());
        final float riskFactor = engine.setPositionGetRiskFactor(clientUUID, uuidAndPosition.getLatitude(), uuidAndPosition.getLongitude(), uuidAndPosition.getAltitude());
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}
