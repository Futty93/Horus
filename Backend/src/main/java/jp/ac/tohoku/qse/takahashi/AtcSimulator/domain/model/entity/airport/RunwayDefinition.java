package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.airport;

public record RunwayDefinition(
        String airportIcao,
        String designator,
        int heading,
        double thresholdLatitude,
        double thresholdLongitude,
        int lengthM) {
}
