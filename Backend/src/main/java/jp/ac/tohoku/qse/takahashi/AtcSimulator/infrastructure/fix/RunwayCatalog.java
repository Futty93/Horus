package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.airport.RunwayDefinition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.GeodeticUtils;

public final class RunwayCatalog {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final double FINAL_INTERCEPT_NM = 8.0;
    private static final double FINAL_EXTENSION_NM = 10.0;
    private static final double NM_PER_METER = 1.0 / 1852.0;

    private final List<RunwayDefinition> runways;

    public RunwayCatalog() {
        this.runways = loadRunways();
    }

    public List<RunwayDefinition> getAllRunways() {
        return Collections.unmodifiableList(runways);
    }

    public List<RunwayDefinition> findByAirportIcao(String icao) {
        if (icao == null || icao.isBlank()) {
            return List.of();
        }
        String key = icao.toUpperCase(Locale.ROOT).trim();
        return runways.stream().filter(r -> r.airportIcao().equals(key)).toList();
    }

    public Optional<RunwayDefinition> findRunway(String airportIcao, String designator) {
        if (airportIcao == null || designator == null) {
            return Optional.empty();
        }
        String icao = airportIcao.toUpperCase(Locale.ROOT).trim();
        String rwy = designator.toUpperCase(Locale.ROOT).trim();
        return runways.stream()
                .filter(r -> r.airportIcao().equals(icao) && r.designator().equals(rwy))
                .findFirst();
    }

    public Optional<FinalApproachGeometry> buildFinalApproachGeometry(RunwayDefinition runway) {
        double approachHeading = normalizeHeading(runway.heading() + 180);
        double[] intercept = GeodeticUtils.offsetPointNm(
                runway.thresholdLatitude(), runway.thresholdLongitude(), approachHeading, FINAL_INTERCEPT_NM);
        double lengthNm = runway.lengthM() * NM_PER_METER;
        double[] departureEnd = GeodeticUtils.offsetPointNm(
                runway.thresholdLatitude(), runway.thresholdLongitude(), runway.heading(), lengthNm);
        double[] extensionEnd = GeodeticUtils.offsetPointNm(
                runway.thresholdLatitude(), runway.thresholdLongitude(), approachHeading, FINAL_EXTENSION_NM);
        return Optional.of(new FinalApproachGeometry(
                runway,
                approachHeading,
                intercept[0],
                intercept[1],
                departureEnd[0],
                departureEnd[1],
                extensionEnd[0],
                extensionEnd[1]));
    }

    public List<Map<String, Object>> toApiMaps() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RunwayDefinition rwy : runways) {
            buildFinalApproachGeometry(rwy).ifPresent(geom -> result.add(geom.toApiMap()));
        }
        return result;
    }

    private static double normalizeHeading(double heading) {
        double h = heading % 360;
        return h < 0 ? h + 360 : h;
    }

    private static List<RunwayDefinition> loadRunways() {
        try (InputStream is = new ClassPathResource("fix/runways.json").getInputStream()) {
            JsonNode root = OBJECT_MAPPER.readTree(is);
            List<RunwayDefinition> list = new ArrayList<>();
            for (JsonNode node : root.get("runways")) {
                list.add(new RunwayDefinition(
                        node.get("airportIcao").asText(),
                        node.get("designator").asText(),
                        node.get("heading").asInt(),
                        node.get("thresholdLatitude").asDouble(),
                        node.get("thresholdLongitude").asDouble(),
                        node.get("lengthM").asInt()));
            }
            return list;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load runways.json", e);
        }
    }

    public record FinalApproachGeometry(
            RunwayDefinition runway,
            double approachCourseHeading,
            double interceptLatitude,
            double interceptLongitude,
            double departureEndLatitude,
            double departureEndLongitude,
            double extensionEndLatitude,
            double extensionEndLongitude) {

        public Map<String, Object> toApiMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("airportIcao", runway.airportIcao());
            map.put("designator", runway.designator());
            map.put("heading", runway.heading());
            map.put("thresholdLatitude", runway.thresholdLatitude());
            map.put("thresholdLongitude", runway.thresholdLongitude());
            map.put("departureEndLatitude", departureEndLatitude);
            map.put("departureEndLongitude", departureEndLongitude);
            map.put("approachCourseHeading", approachCourseHeading);
            map.put("extensionEndLatitude", extensionEndLatitude);
            map.put("extensionEndLongitude", extensionEndLongitude);
            map.put("interceptLatitude", interceptLatitude);
            map.put("interceptLongitude", interceptLongitude);
            return map;
        }
    }
}
