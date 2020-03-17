package org.ftc.server.engine;

import org.ftc.server.db.dao.ConfirmedCasesRepo;
import org.ftc.server.db.dao.ConnectivityRepo;
import org.ftc.server.db.domain.ConfirmedCase;
import org.ftc.server.db.domain.Connectivity;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import lombok.Data;

import static java.time.LocalDateTime.now;
import static java.util.Comparator.comparingDouble;

@Component
public class FtcEngineImpl implements Engine {

    @Autowired
    private GeodeticCalculator geodeticCalculator;

    @Autowired
    private TaskExecutor executor;

    @Autowired
    private ConnectivityRepo connectivityRepo;

    @Autowired
    private ConfirmedCasesRepo confirmedCasesRepo;

    private static final double maximumInfectionDistanceMeters = 4d;
    private static final long maximumInfectionDurationHours = 24 * 21; //21 days
    //one degree of latitude is ca 111 km
    private static final double TenMetersToOneLatitudeDegreeRatio = 0.00008997741d;
    private static final double FiveMetersToOneLatitudeDegreeRatio = 0.00004498870d;

    @Data
    private static class Position {
        private UUID uuid;
        private double latitude;
        private double longitude;
        private double altitude;
    }

    private final List<Position> positions = new ArrayList<>();

    private final Object lock = new Object();

    FtcEngineImpl() {
        Timer timer = new Timer();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                computeAllClosenesses();
            }
        };

        timer.schedule(task, 0, 1000);
    }

    /*TODO
     * This should be ported to hadoop or spark, I'm just pointing out the algorithm here, which is as follows:
     *
     * 1) All the known positions of every user are being sorted by latitude ( is easier to compute that way than longitude, because latitude is a fixed distance )
     * 2) Sublists with all positions in a latitude range of 10 meters are being computed, with the average latitude of each sublist being 5 meters more than the previous subgroup.
     *    This means that all positions are part of two different sublists. Think of the following ( sorted ) list of distances from the latitude origin in meters:
     *             Q = {1, 3, 5, 6, 7, 8, 8, 11, 14, 15, 16, 18, 18, 19, 20}
     *
     *    The sublists would then be:
     *
     *             Q1 = {1, 3, 5, 6, 7, 8, 8                                }
     *             Q2 = {         6, 7, 8, 8, 11, 14, 15                    }
     *             Q3 = {                     11, 14, 15, 16, 18, 18, 19, 20}
     *
     * 3) For every sublist, the distances between all positions in the sublist are being calculated and a set of those position-pairs that are 4 meters or closer to each other is being created.
     *    Lets enrich the first sublist with distances from longitude and altitude origins in meters:
     *
     *          Q1 = {{1, 2, 1}, {3, 3, 4}, {5, 6, 9}, {6, 1, 1}, {7, 2, 5}, {8, 8, 3}, {8, 2, 9}}
     *
     *    I didn't actually crunch the numbers down, but the calculation should yield something like
     *
     *          "positions number 1 and 3, 2 and 3, 2 and 5 are in 4 meters or less proximity to each other"
     *
     * 4) For every position-pair that is in infectious distance, the 'connectivity' is to be calculated, taking in account historic data. The algorithm to do so is not specified,
     *    it must be implemented in a way that the 'Connectivity' between 2 users is always between 0 and 1 and for example two users that have never been 4 meters or closer to
     *    each other have connectivity zero, two users that had been waiting for a bus next to each other a couple of days ago have connectivity 0.15 and a married couple spending
     *    much time together has a connectivity of 0.98.
     */
    private void computeAllClosenesses() {
        synchronized (lock) {
            if(positions.isEmpty()){
                return;
            }

            positions.sort(comparingDouble(Position::getLatitude));

            final double minLatitude = positions.get(0).getLatitude();
            final double maxLatitude = positions.get(positions.size() - 1).getLatitude();

            int positionsIndex = 0;

            for (double batchMedianLatitude = minLatitude; batchMedianLatitude <= maxLatitude; batchMedianLatitude += FiveMetersToOneLatitudeDegreeRatio) {

                final double batchEndLatitude = batchMedianLatitude + TenMetersToOneLatitudeDegreeRatio;

                final List<Position> positionsBatch = new ArrayList<>();

                while (true) {
                    Position candidate = positions.get(positionsIndex++);

                    if (candidate.getLatitude() > batchEndLatitude) {
                        break;
                    }

                    positionsBatch.add(candidate);
                }

                executor.execute(() -> {
                    for (Position positionA : positionsBatch) {
                        for (Position positionB : positionsBatch) {
                            if (positionA == positionB) {
                                continue;
                            }

                            final double distanceInMeters = geodeticCalculator.calculateGeodeticMeasurement(
                                    Ellipsoid.WGS84,
                                    new GlobalPosition(positionA.getLatitude(), positionA.getLongitude(), positionA.getAltitude()),
                                    new GlobalPosition(positionB.getLatitude(), positionB.getLongitude(), positionB.getAltitude())
                            ).getPointToPointDistance();

                            if (distanceInMeters <= maximumInfectionDistanceMeters) {

                                double distanceFactor = distanceInMeters / 4;//distance-factor is between 0 ( no distance ) and 1 (max distance)

                                double newFactor = 1 / (distanceFactor); //newfactor is between 0 ( max distance ) and 1 ( no distance ) -> think 'closeness'

                                Optional<Connectivity> optionalConnectivityFactor = connectivityRepo
                                        .findByUser1IdAndUser2Id(positionA.getUuid(), positionB.getUuid());

                                if (!optionalConnectivityFactor.isPresent()) {
                                    final Connectivity newConnectivity = new Connectivity();

                                    newConnectivity.setFactor(newFactor);
                                    newConnectivity.setUser1Id(positionA.getUuid());
                                    newConnectivity.setUser2Id(positionB.getUuid());

                                    connectivityRepo.save(newConnectivity);
                                } else {
                                    final Connectivity connectivity = optionalConnectivityFactor.get();

                                    final double oldFactor = connectivity.getFactor();

                                    long hoursSinceLastUpdate = Duration.between(connectivity.getLastUpdate(), now()).get(ChronoUnit.HOURS);

                                    //if last contact was more than 21 days ago, then it's no longer of interest and we just use the new factor
                                    if (hoursSinceLastUpdate < maximumInfectionDurationHours) {
                                        //last update was close to 21 days ago -> oldFactorWeight is close to 0
                                        //last update was just now -> oldFactorWeight is close to 1
                                        double oldFactorWeight = 1 - ((double) hoursSinceLastUpdate / (double) maximumInfectionDurationHours);

                                        //increase is between 0 and the difference between newFactor and 1
                                        double newFactorIncrease = (1 - newFactor) * oldFactorWeight * oldFactor;

                                        //the higher oldFactor and oldFactorWeight are, the closer newFactor gets to 1
                                        newFactor += newFactorIncrease;
                                    }

                                    connectivity.setFactor(newFactor);

                                    connectivityRepo.save(connectivity);
                                }
                            }
                        }
                    }
                });
            }

            positions.clear();
        }
    }

    @Override
    public float setPositionGetRiskFactor(UUID userUUID, double latitude, double longitude, double altitude) {
        synchronized (lock) {
            Optional<ConfirmedCase> existingConfirmedCase = confirmedCasesRepo.findByUserUUID(userUUID);

            if (existingConfirmedCase.isPresent()) {
                return 1;
            }

            float riskFactor = 0;

            for (Connectivity connectivityWithConfirmedCase : connectivityRepo.connectivitiesWithConfirmedCases(userUUID)) {
                //TODO is this the statistically correct algorithm?
                //2 connectivities with confirmed cases with a factor of 0.5 each, result in a riskfactor of 0.75
                riskFactor += (1 - riskFactor) * connectivityWithConfirmedCase.getFactor();
            }

            return riskFactor;
        }
    }
}
