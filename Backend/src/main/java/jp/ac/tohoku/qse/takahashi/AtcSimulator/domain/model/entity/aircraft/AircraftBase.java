package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import java.util.Objects;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.ApproachControlReleasedException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.HoldTurnDirection;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.WaypointAltitudeTargetResolver;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.GeodeticUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.IndicatedAirspeedConverter;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.StringUtils;

/**
 * 航空機の基底クラス
 * Strategy パターンとComposition を活用して、異なる航空機タイプの実装を可能にする
 */
public abstract class AircraftBase implements Aircraft {
    protected final Callsign callsign;
    protected AircraftPosition aircraftPosition;
    protected AircraftVector aircraftVector;
    protected InstructedVector instructedVector;
    /** Controller-recorded clearance memo (altitude / heading / speed); does not drive the aircraft. */
    protected InstructedVector atcClearance;
    /** Optional SSR transponder code; null means unassigned. */
    protected String squawk;
    protected final AircraftType aircraftType;

    // Strategy パターンによる飛行動作の委譲
    protected final FlightBehavior flightBehavior;

    // 航空機の物理的特性
    protected final AircraftCharacteristics characteristics;

    protected FlightPlan flightPlan;
    protected int currentWaypointIndex;
    protected NavigationMode navigationMode;
    protected double previousDistanceToWaypoint;
    protected FixPosition directToTarget;
    protected String directToFixName;
    protected boolean resumeFlightPlanAfterDirectTo;
    protected boolean markedForRemoval;
    protected FixPosition holdingFix;
    protected FixPosition holdingOutboundTarget;
    protected HoldTurnDirection holdTurnDirection;
    protected boolean holdingToOutboundLeg;
    protected boolean holdingEntryCompleted;
    protected int resumeFlightPlanIndexAfterHold;
    protected Integer instructedIndicatedAirspeedKnots;
    protected boolean finalApproachCoursePending;
    protected Double finalApproachCourseHeading;
    /** True after a go-around until the next navigation instruction. */
    protected boolean goAroundActive;
    /** True after the approach seat has transferred the aircraft to the tower. */
    protected boolean towerHandoffActive;
    protected FixPosition towerThreshold;

    public static final double MAX_TOWER_HANDOFF_DISTANCE_NM = 10.0;
    public static final double TOWER_HANDOFF_REMOVAL_NM = 0.5;
    private static final int TOWER_HANDOFF_TRACK_HORIZON_SECONDS = 20 * 60;

    private static final double WAYPOINT_PASS_THRESHOLD_MIN_NM = 1.5;
    private static final double WAYPOINT_PASS_THRESHOLD_MAX_NM = 5.0;
    private static final double WAYPOINT_PASS_SECONDS = 5.0;
    private static final double HOLD_OUTBOUND_LEG_DISTANCE_NM = 4.0;
    private static final double HOLD_WAYPOINT_PASS_THRESHOLD_NM = 0.25;
    private static final double HOLD_RIGHT_TURN_FORCE_THRESHOLD_DEG = 45.0;

    private static final boolean WP_DEBUG = "true".equalsIgnoreCase(System.getProperty("flightplan.wp.debug"));

    public AircraftBase(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                       AircraftVector aircraftVector, FlightBehavior flightBehavior,
                       AircraftCharacteristics characteristics) {
        this.callsign = callsign;
        this.aircraftType = aircraftType;
        this.aircraftPosition = aircraftPosition;
        this.aircraftVector = aircraftVector;
        this.flightBehavior = flightBehavior;
        this.characteristics = characteristics;
        this.instructedVector = new InstructedVector(aircraftVector.heading, aircraftPosition.altitude, aircraftVector.groundSpeed);
        this.squawk = null;
        this.flightPlan = null;
        this.currentWaypointIndex = 0;
        this.navigationMode = NavigationMode.HEADING;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        this.directToTarget = null;
        this.directToFixName = null;
        this.resumeFlightPlanAfterDirectTo = false;
        this.markedForRemoval = false;
        this.holdingFix = null;
        this.holdingOutboundTarget = null;
        this.holdTurnDirection = HoldTurnDirection.RIGHT;
        this.holdingToOutboundLeg = false;
        this.holdingEntryCompleted = false;
        this.resumeFlightPlanIndexAfterHold = -1;
    }

    @Override
    public void calculateNextAircraftPosition(double simDeltaSeconds) {
        this.aircraftPosition = flightBehavior.calculateNextPosition(
            this.aircraftPosition,
            this.aircraftVector,
            simDeltaSeconds
        );
    }

    @Override
    public void calculateNextAircraftVector(double simDeltaSeconds) {
        updateInstructedVectorFromNavigation();

        boolean applyRightTurnConstraint = shouldForceRightTurnInHolding();
        var nextHeading = applyRightTurnConstraint
                ? calculateNextHeadingRightTurn(
                        this.aircraftVector.heading.toDouble(),
                        this.instructedVector.instructedHeading.toDouble(),
                        this.characteristics.getMaxTurnRate())
                : flightBehavior.calculateNextHeading(
                        this.aircraftVector.heading.toDouble(),
                        this.instructedVector.instructedHeading.toDouble(),
                        this.characteristics.getMaxTurnRate());

        double targetGroundSpeedKnots = resolveEffectiveTargetGroundSpeedKnots();
        var nextGroundSpeed = flightBehavior.calculateNextGroundSpeed(
            this.aircraftVector.groundSpeed.toDouble(),
            targetGroundSpeedKnots,
            this.characteristics.getMaxAcceleration()
        );

        var nextVerticalSpeed = flightBehavior.calculateNextVerticalSpeed(
            this.aircraftPosition.altitude.toDouble(),
            this.instructedVector.instructedAltitude.toDouble(),
            this.characteristics.getMaxClimbRate(),
            simDeltaSeconds
        );

        this.aircraftVector = new AircraftVector(nextHeading, nextGroundSpeed, nextVerticalSpeed);

        double currentAltitude = this.aircraftPosition.altitude.toDouble();
        double targetAltitude = this.instructedVector.instructedAltitude.toDouble();
        double altitudeDifference = Math.abs(currentAltitude - targetAltitude);

        if (altitudeDifference <= 5.0 && Math.abs(nextVerticalSpeed.toDouble()) <= 50.0) {
            this.aircraftPosition = new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition(
                this.aircraftPosition.latitude,
                this.aircraftPosition.longitude,
                this.instructedVector.instructedAltitude
            );
            this.aircraftVector = new AircraftVector(nextHeading, nextGroundSpeed, new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed(0.0));
        }
    }

    private boolean shouldForceRightTurnInHolding() {
        if (navigationMode != NavigationMode.HOLDING || !holdingEntryCompleted || holdTurnDirection != HoldTurnDirection.RIGHT) {
            return false;
        }
        FixPosition target = resolveNavigationTarget();
        if (target == null) {
            return false;
        }
        double currentHeading = this.aircraftVector.heading.toDouble();
        double targetHeading = flightBehavior.calculateTurnAngle(aircraftPosition, currentHeading, target);
        double clockwiseDelta = GeodeticUtils.normalizeAngle(targetHeading - currentHeading);
        double counterClockwiseDelta = 360.0 - clockwiseDelta;
        return counterClockwiseDelta >= HOLD_RIGHT_TURN_FORCE_THRESHOLD_DEG
                && counterClockwiseDelta < clockwiseDelta;
    }

    private void updateInstructedVectorFromNavigation() {
        if (navigationMode == NavigationMode.HEADING) {
            return;
        }
        FixPosition target = resolveNavigationTarget();
        if (target == null) {
            return;
        }
        double bearing = flightBehavior.calculateTurnAngle(aircraftPosition, aircraftVector.heading.toDouble(), target);
        Altitude targetAltitude = resolveTargetAltitude();
        GroundSpeed targetSpeed = resolveTargetSpeed();
        this.instructedVector = new InstructedVector(new Heading(bearing), targetAltitude, targetSpeed);
    }

    private FixPosition resolveNavigationTarget() {
        if (navigationMode == NavigationMode.DIRECT_TO && directToTarget != null) {
            return directToTarget;
        }
        if (navigationMode == NavigationMode.FLIGHT_PLAN && flightPlan != null) {
            return flightPlan.getNextWaypoint(currentWaypointIndex)
                    .map(FlightPlanWaypoint::getPosition)
                    .orElse(null);
        }
        if (navigationMode == NavigationMode.HOLDING && holdingFix != null) {
            if (!holdingEntryCompleted) {
                return holdingFix;
            }
            return holdingToOutboundLeg ? holdingOutboundTarget : holdingFix;
        }
        return null;
    }

    private Altitude resolveTargetAltitude() {
        if (navigationMode == NavigationMode.DIRECT_TO
                || navigationMode == NavigationMode.HOLDING
                || flightPlan == null) {
            return instructedVector.instructedAltitude;
        }
        double currentAlt = aircraftPosition.altitude.toDouble();
        return flightPlan.getNextWaypoint(currentWaypointIndex)
                .map(wp -> {
                    Altitude resolved = WaypointAltitudeTargetResolver.resolveTarget(wp, currentAlt);
                    if (resolved != null) {
                        return resolved;
                    }
                    return flightPlan.getCruiseAltitude();
                })
                .orElse(instructedVector.instructedAltitude);
    }

    protected double resolveEffectiveTargetGroundSpeedKnots() {
        if (instructedIndicatedAirspeedKnots == null || navigationMode != NavigationMode.HEADING) {
            return instructedVector.instructedGroundSpeed.toDouble();
        }
        return IndicatedAirspeedConverter.indicatedToTrueAirspeedKnots(
                instructedIndicatedAirspeedKnots,
                aircraftPosition.altitude.toDouble());
    }

    private GroundSpeed resolveTargetSpeed() {
        if (navigationMode == NavigationMode.DIRECT_TO
                || navigationMode == NavigationMode.HOLDING
                || flightPlan == null) {
            return instructedVector.instructedGroundSpeed;
        }
        return flightPlan.getNextWaypoint(currentWaypointIndex)
                .map(wp -> wp.getTargetSpeed() != null ? wp.getTargetSpeed() : flightPlan.getCruiseSpeed())
                .orElse(instructedVector.instructedGroundSpeed);
    }

    protected void applyWaypointPassCheck() {
        if (markTowerHandoffArrival()) {
            return;
        }
        FixPosition target = resolveNavigationTarget();
        if (target == null) {
            return;
        }
        double currentDistance = GeodeticUtils.distanceToFix(aircraftPosition, target);
        double threshold = navigationMode == NavigationMode.HOLDING
                ? HOLD_WAYPOINT_PASS_THRESHOLD_NM
                : calculateDynamicThreshold();

        boolean withinThreshold = currentDistance < threshold;
        boolean movingAway = currentDistance > previousDistanceToWaypoint;
        boolean passed = withinThreshold && movingAway;
        previousDistanceToWaypoint = currentDistance;

        if (WP_DEBUG && currentDistance < threshold * 2
                && (navigationMode == NavigationMode.FLIGHT_PLAN || navigationMode == NavigationMode.DIRECT_TO)) {
            String targetName = navigationMode == NavigationMode.DIRECT_TO ? directToFixName : flightPlan.getNextWaypoint(currentWaypointIndex).map(w -> w.getFixName()).orElse("?");
            System.out.printf("[WP_DEBUG] %s %s dist=%.4f NM thresh=%.4f NM within=%s movingAway=%s passed=%s%n",
                    callsign, targetName, currentDistance, threshold, withinThreshold, movingAway, passed);
        }

        if (!withinThreshold || !movingAway) {
            return;
        }

        if (WP_DEBUG) {
            System.out.printf("[WP_DEBUG] %s *** WAYPOINT PASSED ***%n", callsign);
        }

        if (navigationMode == NavigationMode.DIRECT_TO) {
            onDirectToTargetReached();
            return;
        }
        if (navigationMode == NavigationMode.HOLDING) {
            onHoldingTargetReached();
            return;
        }

        if (navigationMode == NavigationMode.FLIGHT_PLAN && flightPlan != null) {
            flightPlan.getNextWaypoint(currentWaypointIndex).ifPresent(this::onWaypointPassed);
        }
    }

    private void onDirectToTargetReached() {
        directToTarget = null;
        previousDistanceToWaypoint = Double.MAX_VALUE;
        if (finalApproachCoursePending && finalApproachCourseHeading != null) {
            finalApproachCoursePending = false;
            navigationMode = NavigationMode.HEADING;
            this.instructedVector = new InstructedVector(
                    new Heading(finalApproachCourseHeading),
                    instructedVector.instructedAltitude,
                    instructedVector.instructedGroundSpeed);
            directToFixName = null;
            resumeFlightPlanAfterDirectTo = false;
            return;
        }
        if (resumeFlightPlanAfterDirectTo && flightPlan != null && directToFixName != null) {
            int idx = flightPlan.findWaypointIndex(directToFixName);
            if (idx >= 0) {
                currentWaypointIndex = idx;
                navigationMode = NavigationMode.FLIGHT_PLAN;
            } else {
                navigationMode = NavigationMode.HEADING;
            }
            resumeFlightPlanAfterDirectTo = false;
        } else {
            navigationMode = NavigationMode.HEADING;
        }
        directToFixName = null;
    }

    private void onWaypointPassed(FlightPlanWaypoint wp) {
        if (!WaypointAltitudeTargetResolver.satisfiesConstraintAtPass(
                wp, aircraftPosition.altitude.toDouble())) {
            return;
        }
        if (wp.shouldRemoveAircraft()) {
            markedForRemoval = true;
            return;
        }
        currentWaypointIndex++;
        previousDistanceToWaypoint = Double.MAX_VALUE;
        if (currentWaypointIndex >= flightPlan.getWaypoints().size()) {
            navigationMode = NavigationMode.HEADING;
        }
    }

    private void onHoldingTargetReached() {
        if (holdingFix == null || holdingOutboundTarget == null) {
            setResumeNavigation();
            return;
        }
        if (!holdingEntryCompleted) {
            holdingEntryCompleted = true;
            holdingToOutboundLeg = true;
            previousDistanceToWaypoint = Double.MAX_VALUE;
            return;
        }
        holdingToOutboundLeg = !holdingToOutboundLeg;
        previousDistanceToWaypoint = Double.MAX_VALUE;
    }

    private double calculateDynamicThreshold() {
        double groundSpeedKnots = aircraftVector.groundSpeed.toDouble();
        double groundSpeedNmPerSec = groundSpeedKnots / 3600.0;
        double threshold = groundSpeedNmPerSec * WAYPOINT_PASS_SECONDS;
        return Math.max(WAYPOINT_PASS_THRESHOLD_MIN_NM, Math.min(threshold, WAYPOINT_PASS_THRESHOLD_MAX_NM));
    }

    @Override
    public boolean shouldBeRemovedFromSimulation() {
        return markedForRemoval;
    }

    @Override
    public double calculateTurnAngle(FixPosition fixPosition) {
        return flightBehavior.calculateTurnAngle(
            this.aircraftPosition,
            this.aircraftVector.heading.toDouble(),
            fixPosition
        );
    }

    @Override
    public boolean isEqualCallsign(Callsign callsign) {
        return this.callsign.equals(callsign);
    }

    // Getters
    @Override
    public Callsign getCallsign() {
        return this.callsign;
    }

    @Override
    public AircraftPosition getAircraftPosition() {
        return this.aircraftPosition;
    }

    @Override
    public AircraftVector getAircraftVector() {
        return this.aircraftVector;
    }

    @Override
    public InstructedVector getInstructedVector() {
        return this.instructedVector;
    }

    @Override
    public AircraftType getAircraftType() {
        return this.aircraftType;
    }

    public AircraftCharacteristics getCharacteristics() {
        return this.characteristics;
    }

    public FlightBehavior getFlightBehavior() {
        return this.flightBehavior;
    }

    // Setters
    public void setAircraftPosition(final AircraftPosition newAircraftPosition) {
        this.aircraftPosition = newAircraftPosition;
    }

    public void setAircraftVector(final AircraftVector newAircraftVector) {
        this.aircraftVector = newAircraftVector;
    }

    public void setInstructedVector(final InstructedVector newInstructedVector) {
        setInstructedVector(newInstructedVector, null);
    }

    public void setInstructedVector(final InstructedVector newInstructedVector, Integer indicatedAirspeedKnots) {
        requireApproachControl();
        this.instructedVector = newInstructedVector;
        this.instructedIndicatedAirspeedKnots = indicatedAirspeedKnots;
    }

    public Integer getInstructedIndicatedAirspeedKnots() {
        return instructedIndicatedAirspeedKnots;
    }

    public boolean isGoAroundActive() {
        return goAroundActive;
    }

    public boolean isTowerHandoffActive() {
        return towerHandoffActive;
    }

    /**
     * Release the aircraft to the tower without changing the current heading, altitude, or speed target.
     * The aircraft leaves the simulation once it reaches the runway threshold.
     */
    public void beginTowerHandoff(double thresholdLatitude, double thresholdLongitude) {
        requireApproachControl();
        if (goAroundActive) {
            throw new IllegalArgumentException("Cancel the go-around before handing off to the tower");
        }
        FixPosition threshold = new FixPosition(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude(thresholdLatitude),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude(thresholdLongitude));
        double distanceNm = GeodeticUtils.distanceToFix(aircraftPosition, threshold);
        if (navigationMode != NavigationMode.HEADING) {
            throw new IllegalArgumentException(
                    "Establish on the final approach course before tower handoff");
        }
        if (distanceNm > MAX_TOWER_HANDOFF_DISTANCE_NM) {
            throw new IllegalArgumentException(
                    "Aircraft is too far from the runway for tower handoff ("
                            + String.format("%.1f", distanceNm) + " NM, max "
                            + MAX_TOWER_HANDOFF_DISTANCE_NM + " NM)");
        }
        if (distanceNm > TOWER_HANDOFF_REMOVAL_NM
                && !maintainedTrackReachesThreshold(thresholdLatitude, thresholdLongitude)) {
            throw new IllegalArgumentException(
                    "Aircraft track does not reach the runway threshold");
        }
        this.towerThreshold = threshold;
        this.towerHandoffActive = true;
    }

    /**
     * The handoff keeps the current heading and speed targets. Accept only when that path
     * enters the removal radius, including the turn onto the instructed heading.
     */
    private boolean maintainedTrackReachesThreshold(double thresholdLatitude, double thresholdLongitude) {
        double lat = aircraftPosition.latitude.toDouble();
        double lon = aircraftPosition.longitude.toDouble();
        double heading = aircraftVector.heading.toDouble();
        double speed = aircraftVector.groundSpeed.toDouble();
        double targetHeading = instructedVector.instructedHeading.toDouble();
        double targetSpeed = instructedVector.instructedGroundSpeed.toDouble();
        double previous = horizontalDistanceNm(lat, lon, thresholdLatitude, thresholdLongitude);
        double minimum = previous;
        int recedingSeconds = 0;
        for (int second = 0; second < TOWER_HANDOFF_TRACK_HORIZON_SECONDS; second++) {
            heading = flightBehavior.calculateNextHeading(heading, targetHeading, characteristics.getMaxTurnRate())
                    .toDouble();
            speed = PositionUtils.calculateNextGroundSpeed(speed, targetSpeed, characteristics.getMaxAcceleration())
                    .toDouble();
            double stepNm = Math.max(0.0, speed) / 3600.0;
            double headingRad = Math.toRadians(heading);
            lat += (stepNm * Math.cos(headingRad)) / 60.0;
            double cosLat = Math.cos(Math.toRadians(lat));
            lon += cosLat == 0.0 ? 0.0 : (stepNm * Math.sin(headingRad)) / (60.0 * cosLat);
            double distanceNm = horizontalDistanceNm(lat, lon, thresholdLatitude, thresholdLongitude);
            if (distanceNm <= TOWER_HANDOFF_REMOVAL_NM) {
                return true;
            }
            minimum = Math.min(minimum, distanceNm);
            boolean aligned = smallestHeadingDelta(heading, targetHeading) < 1.0;
            if (aligned && distanceNm > previous) {
                recedingSeconds++;
                if (recedingSeconds >= 5) {
                    return minimum <= TOWER_HANDOFF_REMOVAL_NM;
                }
            } else {
                recedingSeconds = 0;
            }
            previous = distanceNm;
        }
        return minimum <= TOWER_HANDOFF_REMOVAL_NM;
    }

    private static double horizontalDistanceNm(double latitude, double longitude,
                                               double thresholdLatitude, double thresholdLongitude) {
        AircraftPosition from = new AircraftPosition(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude(latitude),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude(longitude),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude(0));
        FixPosition threshold = new FixPosition(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude(thresholdLatitude),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude(thresholdLongitude));
        return GeodeticUtils.distanceToFix(from, threshold);
    }

    /** @return true when the aircraft has reached the threshold and is marked for removal */
    public boolean markTowerHandoffArrival() {
        if (!towerHandoffActive || towerThreshold == null) {
            return false;
        }
        double distanceNm = GeodeticUtils.distanceToFix(aircraftPosition, towerThreshold);
        if (distanceNm <= TOWER_HANDOFF_REMOVAL_NM) {
            markedForRemoval = true;
            return true;
        }
        return false;
    }

    /**
     * Abandon the approach and climb on the runway heading.
     * Keeps the current ground-speed target and any indicated-airspeed instruction.
     */
    public void beginGoAround(double runwayHeading, double climbAltitudeFeet) {
        requireApproachControl();
        double currentAlt = aircraftPosition.altitude.toDouble();
        if (climbAltitudeFeet < currentAlt + 500.0) {
            throw new IllegalArgumentException(
                    "Go-around climb altitude must be at least 500 ft above current altitude");
        }
        setNavigationMode(NavigationMode.HEADING);
        clearHoldingState();
        this.directToTarget = null;
        this.directToFixName = null;
        this.resumeFlightPlanAfterDirectTo = false;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        this.instructedVector = new InstructedVector(
                new Heading(runwayHeading),
                new Altitude(climbAltitudeFeet),
                this.instructedVector.instructedGroundSpeed);
        this.goAroundActive = true;
    }

    public void joinFinalApproachIntercept(FixPosition interceptPoint, String fixLabel, double approachCourseHeading) {
        requireApproachControl();
        clearFinalApproachInterceptState();
        finalApproachCoursePending = true;
        finalApproachCourseHeading = approachCourseHeading;
        applyDirectToNavigation(interceptPoint, fixLabel, false);
    }

    private void clearFinalApproachInterceptState() {
        finalApproachCoursePending = false;
        finalApproachCourseHeading = null;
    }

    public InstructedVector getAtcClearance() {
        return atcClearance;
    }

    public void setAtcClearance(final InstructedVector clearance) {
        requireApproachControl();
        this.atcClearance = clearance;
    }

    public boolean hasAtcClearance() {
        return atcClearance != null;
    }

    public String getSquawk() {
        return squawk;
    }

    public void setSquawk(String squawk) {
        requireApproachControl();
        this.squawk = squawk;
    }

    public void setFlightPlan(FlightPlan flightPlan) {
        requireApproachControl();
        clearGoAroundActive();
        this.flightPlan = flightPlan;
        this.currentWaypointIndex = 0;
        this.navigationMode = flightPlan != null ? NavigationMode.FLIGHT_PLAN : NavigationMode.HEADING;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        clearHoldingState();
    }

    public void setDirectTo(FixPosition target, String fixName, boolean resumeFlightPlan) {
        requireApproachControl();
        clearFinalApproachInterceptState();
        applyDirectToNavigation(target, fixName, resumeFlightPlan);
    }

    private void applyDirectToNavigation(FixPosition target, String fixName, boolean resumeFlightPlan) {
        clearGoAroundActive();
        this.directToTarget = target;
        this.directToFixName = fixName;
        this.resumeFlightPlanAfterDirectTo = resumeFlightPlan;
        this.navigationMode = NavigationMode.DIRECT_TO;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        clearHoldingState();
    }

    public void setHoldAtFix(FixPosition fix, String fixName, HoldTurnDirection turnDirection) {
        requireApproachControl();
        clearGoAroundActive();
        clearFinalApproachInterceptState();
        this.holdingFix = fix;
        this.holdTurnDirection = turnDirection;
        this.holdingToOutboundLeg = false;
        this.holdingEntryCompleted = false;
        this.holdingOutboundTarget = createHoldingOutboundTarget(fix, fixName);
        this.resumeFlightPlanIndexAfterHold = computeResumeFlightPlanIndexAfterHold(fixName);
        this.navigationMode = NavigationMode.HOLDING;
        this.directToTarget = null;
        this.directToFixName = null;
        this.resumeFlightPlanAfterDirectTo = false;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
    }

    public void setResumeNavigation() {
        requireApproachControl();
        clearGoAroundActive();
        clearFinalApproachInterceptState();
        if (flightPlan != null) {
            if (resumeFlightPlanIndexAfterHold >= flightPlan.getWaypoints().size()) {
                this.navigationMode = NavigationMode.HEADING;
                clearHoldingState();
                return;
            }
            if (resumeFlightPlanIndexAfterHold >= 0) {
                this.currentWaypointIndex = resumeFlightPlanIndexAfterHold;
            }
            this.navigationMode = NavigationMode.FLIGHT_PLAN;
            this.directToTarget = null;
            this.directToFixName = null;
            this.resumeFlightPlanAfterDirectTo = false;
            clearHoldingState();
        }
    }

    private FixPosition createHoldingOutboundTarget(FixPosition fix, String fixName) {
        double inboundBearing = calculateDeterministicInboundBearing(fix, fixName);
        double outboundBearing = GeodeticUtils.normalizeAngle(inboundBearing + 180.0);
        return offsetFix(fix, outboundBearing, HOLD_OUTBOUND_LEG_DISTANCE_NM);
    }

    private double calculateDeterministicInboundBearing(FixPosition fix, String fixName) {
        if (fixName != null && !fixName.isBlank()) {
            return Math.floorMod(fixName.trim().toUpperCase().hashCode(), 360);
        }
        int latMilli = (int) Math.round(fix.latitude.toDouble() * 1000.0);
        int lonMilli = (int) Math.round(fix.longitude.toDouble() * 1000.0);
        return Math.floorMod(Objects.hash(latMilli, lonMilli), 360);
    }

    private FixPosition offsetFix(FixPosition origin, double bearingDeg, double distanceNm) {
        double bearingRad = Math.toRadians(bearingDeg);
        double latDeg = origin.latitude.toDouble();
        double lonDeg = origin.longitude.toDouble();
        double deltaLatDeg = (distanceNm * Math.cos(bearingRad)) / 60.0;
        double cosLat = Math.cos(Math.toRadians(latDeg));
        double deltaLonDeg = cosLat == 0.0 ? 0.0 : (distanceNm * Math.sin(bearingRad)) / (60.0 * cosLat);
        return new FixPosition(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude(latDeg + deltaLatDeg),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude(lonDeg + deltaLonDeg));
    }

    private void clearGoAroundActive() {
        this.goAroundActive = false;
    }

    private void requireApproachControl() {
        if (towerHandoffActive) {
            throw new ApproachControlReleasedException(callsign.toString());
        }
    }

    private static double smallestHeadingDelta(double left, double right) {
        double delta = Math.abs(GeodeticUtils.normalizeAngle(left) - GeodeticUtils.normalizeAngle(right));
        return Math.min(delta, 360.0 - delta);
    }

    private void clearHoldingState() {
        this.holdingFix = null;
        this.holdingOutboundTarget = null;
        this.holdTurnDirection = HoldTurnDirection.RIGHT;
        this.holdingToOutboundLeg = false;
        this.holdingEntryCompleted = false;
        this.resumeFlightPlanIndexAfterHold = -1;
    }

    private int computeResumeFlightPlanIndexAfterHold(String fixName) {
        if (flightPlan == null) {
            return -1;
        }
        if (fixName == null || fixName.isBlank()) {
            return currentWaypointIndex;
        }
        int holdFixIndex = flightPlan.findWaypointIndex(fixName);
        if (holdFixIndex < 0) {
            return currentWaypointIndex;
        }
        int nextIndex = holdFixIndex + 1;
        int waypointCount = flightPlan.getWaypoints().size();
        if (waypointCount <= 0) {
            return -1;
        }
        if (nextIndex >= waypointCount) {
            return waypointCount;
        }
        return nextIndex;
    }

    private Heading calculateNextHeadingRightTurn(double currentHeading, double targetHeading, double maxTurnRate) {
        double clockwiseDelta = GeodeticUtils.normalizeAngle(targetHeading - currentHeading);
        double turnAmount = Math.min(maxTurnRate, clockwiseDelta);
        return new Heading(GeodeticUtils.normalizeAngle(currentHeading + turnAmount));
    }

    public void setNavigationMode(NavigationMode mode) {
        requireApproachControl();
        clearGoAroundActive();
        if (mode != NavigationMode.DIRECT_TO) {
            clearFinalApproachInterceptState();
        }
        this.navigationMode = mode;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }

    public NavigationMode getNavigationMode() {
        return navigationMode;
    }

    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }

    public void setCurrentWaypointIndex(int index) {
        requireApproachControl();
        this.currentWaypointIndex = index;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
    }

    /**
     * 航空機の詳細情報を取得（デバッグ用）
     */
    public String getDetailedInfo() {
        return String.format("%s - %s (Category: %s, MaxSpeed: %.1f knots)",
            callsign, aircraftType, characteristics.getCategory().getDescription(), characteristics.getMaxSpeed());
    }

    /**
     * フロントエンドのレーダー表示用フォーマットで航空機情報を出力
     * 既存のフロントエンドとの互換性を保つため
     */
    public String toRadarString() {
        return StringUtils.formatAircraftBaseInfo(
            callsign.toString(),
            aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
            aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
            instructedVector.instructedHeading.toDouble(), instructedVector.instructedGroundSpeed.toDouble(), instructedVector.instructedAltitude.toDouble(),
            getAircraftCategory(), aircraftType.toString()
        ) + "}";
    }

    /**
     * 航空機カテゴリを取得（サブクラスでオーバーライド可能）
     */
    protected String getAircraftCategory() {
        return characteristics.getCategory().name();
    }
}
