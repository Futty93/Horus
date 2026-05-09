package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.constants.AtcSimulatorConstants.REFRESH_RATE;

import java.util.Objects;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.HoldTurnDirection;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
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
    protected String holdingFixName;
    protected FixPosition holdingOutboundTarget;
    protected HoldTurnDirection holdTurnDirection;
    protected boolean holdingToOutboundLeg;
    protected int resumeFlightPlanIndexAfterHold;

    private static final double WAYPOINT_PASS_THRESHOLD_MIN_NM = 1.5;
    private static final double WAYPOINT_PASS_THRESHOLD_MAX_NM = 5.0;
    private static final double WAYPOINT_PASS_SECONDS = 5.0;
    private static final double HOLD_OUTBOUND_LEG_DISTANCE_NM = 4.0;
    private static final double HOLD_WAYPOINT_PASS_THRESHOLD_NM = 0.25;

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
        this.holdingFixName = null;
        this.holdingOutboundTarget = null;
        this.holdTurnDirection = HoldTurnDirection.RIGHT;
        this.holdingToOutboundLeg = false;
        this.resumeFlightPlanIndexAfterHold = -1;
    }

    @Override
    public void calculateNextAircraftPosition() {
        this.aircraftPosition = flightBehavior.calculateNextPosition(
            this.aircraftPosition,
            this.aircraftVector,
            REFRESH_RATE
        );
    }

    @Override
    public void calculateNextAircraftVector() {
        updateInstructedVectorFromNavigation();

        var nextHeading = (navigationMode == NavigationMode.HOLDING && holdTurnDirection == HoldTurnDirection.RIGHT)
                ? calculateNextHeadingRightTurn(
                        this.aircraftVector.heading.toDouble(),
                        this.instructedVector.instructedHeading.toDouble(),
                        this.characteristics.getMaxTurnRate())
                : flightBehavior.calculateNextHeading(
                        this.aircraftVector.heading.toDouble(),
                        this.instructedVector.instructedHeading.toDouble(),
                        this.characteristics.getMaxTurnRate());

        var nextGroundSpeed = flightBehavior.calculateNextGroundSpeed(
            this.aircraftVector.groundSpeed.toDouble(),
            this.instructedVector.instructedGroundSpeed.toDouble(),
            this.characteristics.getMaxAcceleration()
        );

        var nextVerticalSpeed = flightBehavior.calculateNextVerticalSpeed(
            this.aircraftPosition.altitude.toDouble(),
            this.instructedVector.instructedAltitude.toDouble(),
            this.characteristics.getMaxClimbRate(),
            REFRESH_RATE
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
        return flightPlan.getNextWaypoint(currentWaypointIndex)
                .map(wp -> wp.getTargetAltitude() != null ? wp.getTargetAltitude() : flightPlan.getCruiseAltitude())
                .orElse(instructedVector.instructedAltitude);
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
        this.instructedVector = newInstructedVector;
    }

    public InstructedVector getAtcClearance() {
        return atcClearance;
    }

    public void setAtcClearance(final InstructedVector clearance) {
        this.atcClearance = clearance;
    }

    public boolean hasAtcClearance() {
        return atcClearance != null;
    }

    public String getSquawk() {
        return squawk;
    }

    public void setSquawk(String squawk) {
        this.squawk = squawk;
    }

    public void setFlightPlan(FlightPlan flightPlan) {
        this.flightPlan = flightPlan;
        this.currentWaypointIndex = 0;
        this.navigationMode = flightPlan != null ? NavigationMode.FLIGHT_PLAN : NavigationMode.HEADING;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        clearHoldingState();
    }

    public void setDirectTo(FixPosition target, String fixName, boolean resumeFlightPlan) {
        this.directToTarget = target;
        this.directToFixName = fixName;
        this.resumeFlightPlanAfterDirectTo = resumeFlightPlan;
        this.navigationMode = NavigationMode.DIRECT_TO;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        clearHoldingState();
    }

    public void setHoldAtFix(FixPosition fix, String fixName, HoldTurnDirection turnDirection) {
        this.holdingFix = fix;
        this.holdingFixName = fixName;
        this.holdTurnDirection = turnDirection;
        this.holdingToOutboundLeg = false;
        this.holdingOutboundTarget = createHoldingOutboundTarget(fix, fixName);
        this.resumeFlightPlanIndexAfterHold = computeResumeFlightPlanIndexAfterHold(fixName);
        this.navigationMode = NavigationMode.HOLDING;
        this.directToTarget = null;
        this.directToFixName = null;
        this.resumeFlightPlanAfterDirectTo = false;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
    }

    public void setResumeNavigation() {
        if (flightPlan != null) {
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

    private void clearHoldingState() {
        this.holdingFix = null;
        this.holdingFixName = null;
        this.holdingOutboundTarget = null;
        this.holdTurnDirection = HoldTurnDirection.RIGHT;
        this.holdingToOutboundLeg = false;
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
        int maxIndex = flightPlan.getWaypoints().size() - 1;
        if (maxIndex < 0) {
            return -1;
        }
        return Math.min(nextIndex, maxIndex);
    }

    private Heading calculateNextHeadingRightTurn(double currentHeading, double targetHeading, double maxTurnRate) {
        double clockwiseDelta = GeodeticUtils.normalizeAngle(targetHeading - currentHeading);
        double turnAmount = Math.min(maxTurnRate, clockwiseDelta);
        return new Heading(GeodeticUtils.normalizeAngle(currentHeading + turnAmount));
    }

    public void setNavigationMode(NavigationMode mode) {
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
