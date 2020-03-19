package org.ftc.server;

import com.google.protobuf.Empty;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.ftc.Ftc;
import org.ftc.ServiceGrpc;
import org.ftc.server.db.dao.UserRepository;
import org.ftc.server.db.domain.User;
import org.ftc.server.engine.Engine;
import org.ftc.server.engine.UUIDVerifier;
import org.ftc.server.firewall.RequestLimitingInterceptor;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

import io.grpc.stub.StreamObserver;

@GRpcService(interceptors = RequestLimitingInterceptor.class)
public class FtcService extends ServiceGrpc.ServiceImplBase {

    @Autowired
    private Engine engine;

    @Autowired
    private UUIDVerifier uuidVerifier;

    @Autowired
    private String messagingServiceSid;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecureRandom secureRandom;

    @Override
    @Transactional
    public void requestUUID(Ftc.PhoneNumber request, StreamObserver<Empty> responseObserver) {
        try {
            if (userRepository.existsByPhoneNumber(request.getE164())) {
                throw new IllegalArgumentException();
            }

            final UUID uuid = new UUID(secureRandom.nextLong(), secureRandom.nextLong());

            final Message message = Message.creator(new PhoneNumber(request.getE164()), messagingServiceSid, uuid.toString()).create();

            //TODO is this correct?
            switch (message.getStatus()){
                case SENT:
                case DELIVERED:
                case SENDING:
                case ACCEPTED:
                    break;
                default:
                    //TODO custom exception
                    throw new Exception();
            }

            User user = new User();
            user.setId(uuid);
            user.setPhoneNumber(request.getE164());

            userRepository.save(user);

            responseObserver.onNext(Empty.getDefaultInstance());
        } catch (Throwable t) {
            responseObserver.onError(t);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void updatePositionGetRiskEstimation(Ftc.UUIDAndPosition request, StreamObserver<Ftc.RiskEstimation> responseObserver) {

        try {
            final UUID uuid = UUID.fromString(request.getUUID());

            if (!uuidVerifier.verify(uuid)) {
                throw new IllegalArgumentException("unknown UUID");
            }

            final float riskFactor = engine.setPositionGetRiskFactor(uuid, request.getLatitude(), request.getLongitude(), request.getAltitude());

            responseObserver.onNext(Ftc.RiskEstimation.newBuilder().setRiskFactor(riskFactor).build());
        } catch (Throwable t) {
            responseObserver.onError(t);
        } finally {
            responseObserver.onCompleted();
        }
    }
}
