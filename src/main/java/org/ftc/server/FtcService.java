package org.ftc.server;

import org.ftc.Ftc;
import org.ftc.ServiceGrpc;
import org.ftc.server.engine.Engine;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import io.grpc.stub.StreamObserver;

@GRpcService
public class FtcService extends ServiceGrpc.ServiceImplBase {

    @Autowired
    private Engine engine;

    @Override
    public void updatePositionGetRiskEstimation(Ftc.UUIDAndPosition request, StreamObserver<Ftc.RiskEstimation> responseObserver) {

        try {
            final UUID uuid = UUID.fromString(request.getUUID());

            final float riskFactor = engine.setPositionGetRiskFactor(uuid, request.getLatitude(), request.getLongitude(), request.getAltitude());

            responseObserver.onNext(Ftc.RiskEstimation.newBuilder().setRiskFactor(riskFactor).build());
            responseObserver.onCompleted();
        } catch (Throwable t){
            responseObserver.onError(t);
        }
    }
}
