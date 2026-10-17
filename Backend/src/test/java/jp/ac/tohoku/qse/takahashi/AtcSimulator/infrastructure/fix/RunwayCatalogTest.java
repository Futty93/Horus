package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.GeodeticUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;

class RunwayCatalogTest {

    @Test
    void runwayLengthMatchesThresholdToDepartureEnd() {
        RunwayCatalog catalog = new RunwayCatalog();
        var rwy = catalog.findRunway("RJTT", "16R").orElseThrow();
        var geom = catalog.buildFinalApproachGeometry(rwy).orElseThrow();
        AircraftPosition threshold = new AircraftPosition(
                new Latitude(rwy.thresholdLatitude()),
                new Longitude(rwy.thresholdLongitude()),
                new Altitude(0));
        AircraftPosition departureEnd = new AircraftPosition(
                new Latitude(geom.departureEndLatitude()),
                new Longitude(geom.departureEndLongitude()),
                new Altitude(0));
        double nm = GeodeticUtils.calculateHorizontalDistance(threshold, departureEnd);
        double expectedNm = rwy.lengthM() / 1852.0;
        assertTrue(Math.abs(nm - expectedNm) < 0.05, "runway length " + nm + " vs " + expectedNm);
    }

    @Test
    void inboundApproachCourseUsesRunwayHeadingNotReciprocal() {
        RunwayCatalog catalog = new RunwayCatalog();
        var rwy = catalog.findRunway("RJTT", "16R").orElseThrow();
        var geom = catalog.buildFinalApproachGeometry(rwy).orElseThrow();
        assertEquals(163, geom.approachCourseHeading(), 0.01);
    }

    @Test
    void interceptLiesOnOutboundExtensionFromThreshold() {
        RunwayCatalog catalog = new RunwayCatalog();
        var rwy = catalog.findRunway("RJTT", "16R").orElseThrow();
        var geom = catalog.buildFinalApproachGeometry(rwy).orElseThrow();
        AircraftPosition threshold = new AircraftPosition(
                new Latitude(rwy.thresholdLatitude()),
                new Longitude(rwy.thresholdLongitude()),
                new Altitude(0));
        AircraftPosition intercept = new AircraftPosition(
                new Latitude(geom.interceptLatitude()),
                new Longitude(geom.interceptLongitude()),
                new Altitude(0));
        double bearing = PositionUtils.calculateBearingToTarget(
                threshold,
                intercept.latitude.toDouble(),
                intercept.longitude.toDouble());
        assertEquals(343, bearing, 5.0);
        double interceptNm = GeodeticUtils.calculateHorizontalDistance(threshold, intercept);
        assertEquals(8.0, interceptNm, 0.1);
    }
}
