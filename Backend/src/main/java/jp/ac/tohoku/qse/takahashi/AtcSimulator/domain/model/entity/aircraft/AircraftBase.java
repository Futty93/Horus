package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.WaypointAction;
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
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.StringUtils;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.constants.AtcSimulatorConstants.REFRESH_RATE;

/**
 * 航空機の基底クラス
 * Strategy パターンとComposition を活用して、異なる航空機タイプの実装を可能にする
 */
public abstract class AircraftBase implements Aircraft {
    protected final Callsign callsign;
    protected AircraftPosition aircraftPosition;
    protected AircraftVector aircraftVector;
    protected InstructedVector instructedVector;
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

    private static final double WAYPOINT_PASS_THRESHOLD_MIN_NM = 1.5;
    private static final double WAYPOINT_PASS_THRESHOLD_MAX_NM = 5.0;
    private static final double WAYPOINT_PASS_SECONDS = 5.0;

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
        this.flightPlan = null;
        this.currentWaypointIndex = 0;
        this.navigationMode = NavigationMode.HEADING;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
        this.directToTarget = null;
        this.directToFixName = null;
        this.resumeFlightPlanAfterDirectTo = false;
        this.markedForRemoval = false;
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

        var nextHeading = flightBehavior.calculateNextHeading(
            this.aircraftVector.heading.toDouble(),
            this.instructedVector.instructedHeading.toDouble(),
            this.characteristics.getMaxTurnRate()
        );

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
        return null;
    }

    private Altitude resolveTargetAltitude() {
        if (navigationMode == NavigationMode.DIRECT_TO || flightPlan == null) {
            return instructedVector.instructedAltitude;
        }
        return flightPlan.getNextWaypoint(currentWaypointIndex)
                .map(wp -> wp.getTargetAltitude() != null ? wp.getTargetAltitude() : flightPlan.getCruiseAltitude())
                .orElse(instructedVector.instructedAltitude);
    }

    private GroundSpeed resolveTargetSpeed() {
        if (navigationMode == NavigationMode.DIRECT_TO || flightPlan == null) {
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
        double threshold = calculateDynamicThreshold();

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
            }
            navigationMode = NavigationMode.FLIGHT_PLAN;
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

    public void setFlightPlan(FlightPlan flightPlan) {
        this.flightPlan = flightPlan;
        this.currentWaypointIndex = 0;
        this.navigationMode = flightPlan != null ? NavigationMode.FLIGHT_PLAN : NavigationMode.HEADING;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
    }

    public void setDirectTo(FixPosition target, String fixName, boolean resumeFlightPlan) {
        this.directToTarget = target;
        this.directToFixName = fixName;
        this.resumeFlightPlanAfterDirectTo = resumeFlightPlan;
        this.navigationMode = NavigationMode.DIRECT_TO;
        this.previousDistanceToWaypoint = Double.MAX_VALUE;
    }

    public void setResumeNavigation() {
        if (flightPlan != null) {
            this.navigationMode = NavigationMode.FLIGHT_PLAN;
            this.directToTarget = null;
            this.directToFixName = null;
            this.resumeFlightPlanAfterDirectTo = false;
        }
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
