package org.ftc.server.engine;

import java.util.UUID;

public interface Engine {
    float setPositionGetRiskFactor(UUID userUUID, double latitude, double longitude, double altitude);
}
