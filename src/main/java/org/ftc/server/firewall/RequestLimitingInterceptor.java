package org.ftc.server.firewall;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

//TODO die Anzahl der erlaubten API-zugriffe sollte pro Zeiteinheit begrenzt werden
//updatePositionGetRiskEstimation -> 1 Zugriff alle 10 Sekunden
//requestUUID -> 3 Zugriffe pro Tag
public class RequestLimitingInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        return new ServerCall.Listener<ReqT>() {
            @Override
            public void onMessage(ReqT message) {
                super.onMessage(message);
            }
        };
    }
}
