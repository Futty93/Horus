package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.WakeTurbulenceCategory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.GeodeticUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;

/**
 * Minimum horizontal separation for a pair: terminal 3 NM, en-route 5 NM, wake when in trail.
 */
public final class HorizontalSeparationRequirements {

    public static final double ENROUTE_HORIZONTAL_NM = 5.0;
    public static final double TERMINAL_HORIZONTAL_NM = 3.0;
    public static final double TERMINAL_AREA_RADIUS_NM = 40.0;
    /** Default terminal radar site until scenario defines one (Haneda). */
    public static final double DEFAULT_RADAR_SITE_LAT = 35.5494;
    public static final double DEFAULT_RADAR_SITE_LON = 139.7798;

    private static final double WAKE_TRAIL_MAX_NM = 15.0;
    private static final double WAKE_HEADING_TOLERANCE_DEG = 30.0;
    private static final double WAKE_VERTICAL_MAX_FT = 1000.0;

    private HorizontalSeparationRequirements() {
    }

    public static double requiredHorizontalSeparationNm(Aircraft aircraft1, Aircraft aircraft2) {
        double base = baseSeparationNm(aircraft1, aircraft2);
        double wake = wakeTrailSeparationNm(aircraft1, aircraft2);
        return Math.max(base, wake);
    }

    private static double baseSeparationNm(Aircraft a1, Aircraft a2) {
        if (insideTerminalArea(a1) && insideTerminalArea(a2)) {
            return TERMINAL_HORIZONTAL_NM;
        }
        return ENROUTE_HORIZONTAL_NM;
    }

    private static boolean insideTerminalArea(Aircraft aircraft) {
        return distanceToRadarSiteNm(aircraft.getAircraftPosition()) <= TERMINAL_AREA_RADIUS_NM;
    }

    static double distanceToRadarSiteNm(AircraftPosition position) {
        AircraftPosition radar = new AircraftPosition(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude(
                        DEFAULT_RADAR_SITE_LAT),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude(
                        DEFAULT_RADAR_SITE_LON),
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude(0));
        return GeodeticUtils.calculateHorizontalDistance(position, radar);
    }

    private static double wakeTrailSeparationNm(Aircraft a1, Aircraft a2) {
        WakePair pair = identifyWakeLeaderFollower(a1, a2);
        if (pair == null) {
            return 0;
        }
        return wakeSeparationTableNm(pair.leaderCategory(), pair.followerCategory());
    }

    private record WakePair(WakeTurbulenceCategory leaderCategory, WakeTurbulenceCategory followerCategory) {
    }

    private static WakePair identifyWakeLeaderFollower(Aircraft a1, Aircraft a2) {
        double horizontalNm = GeodeticUtils.calculateHorizontalDistance(
                a1.getAircraftPosition(), a2.getAircraftPosition());
        if (horizontalNm > WAKE_TRAIL_MAX_NM) {
            return null;
        }
        double verticalFt = GeodeticUtils.calculateVerticalDistance(
                a1.getAircraftPosition(), a2.getAircraftPosition());
        if (verticalFt > WAKE_VERTICAL_MAX_FT) {
            return null;
        }

        if (isFollower(a1, a2)) {
            return new WakePair(
                    WakeTurbulenceCategory.fromAircraftType(a1.getAircraftType().toString()),
                    WakeTurbulenceCategory.fromAircraftType(a2.getAircraftType().toString()));
        }
        if (isFollower(a2, a1)) {
            return new WakePair(
                    WakeTurbulenceCategory.fromAircraftType(a2.getAircraftType().toString()),
                    WakeTurbulenceCategory.fromAircraftType(a1.getAircraftType().toString()));
        }
        return null;
    }

    /** Returns true when {@code follower} is behind {@code leader} on a similar track. */
    private static boolean isFollower(Aircraft leader, Aircraft follower) {
        double leaderHeading = leader.getAircraftVector().heading.toDouble();
        double bearingFromLeaderToFollower = PositionUtils.calculateBearingToTarget(
                leader.getAircraftPosition(), follower.getAircraftPosition().latitude.toDouble(),
                follower.getAircraftPosition().longitude.toDouble());
        double relativeBearing = GeodeticUtils.normalizeAngle(bearingFromLeaderToFollower - leaderHeading);
        if (relativeBearing < 135 || relativeBearing > 225) {
            return false;
        }
        double headingDiff = Math.abs(
                GeodeticUtils.normalizeAngle(follower.getAircraftVector().heading.toDouble() - leaderHeading));
        return headingDiff <= WAKE_HEADING_TOLERANCE_DEG
                || headingDiff >= 360 - WAKE_HEADING_TOLERANCE_DEG;
    }

    static double wakeSeparationTableNm(WakeTurbulenceCategory leader, WakeTurbulenceCategory follower) {
        if (leader == WakeTurbulenceCategory.HEAVY) {
            return switch (follower) {
                case HEAVY -> 4.0;
                case MEDIUM -> 5.0;
                case LIGHT -> 6.0;
            };
        }
        if (leader == WakeTurbulenceCategory.MEDIUM && follower == WakeTurbulenceCategory.LIGHT) {
            return 5.0;
        }
        return 0;
    }
}
