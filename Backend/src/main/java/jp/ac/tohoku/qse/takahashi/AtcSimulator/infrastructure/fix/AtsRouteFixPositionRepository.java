package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.FixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RadioNavigationAid;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Route;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RoutePoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Waypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

public class AtsRouteFixPositionRepository implements FixPositionRepository {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<Waypoint> waypoints;
    private final List<RadioNavigationAid> radioNavigationAids;
    private final List<Route> atsLowerRoutes;
    private final List<Route> rnavRoutes;
    private final Map<String, double[]> airportPositions;
    private final Map<String, String> icaoToIata;

    public AtsRouteFixPositionRepository() {
        try {
            this.waypoints = loadWaypoints();
            this.radioNavigationAids = loadRadioNavigationAids();
            this.atsLowerRoutes = loadAtsLowerRoutes();
            this.rnavRoutes = loadRnavRoutes();
            var airportData = loadAirportData();
            this.airportPositions = airportData.positions();
            this.icaoToIata = airportData.icaoToIata();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load fix data from classpath", e);
        }
    }

    public List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public List<RadioNavigationAid> getRadioNavigationAids() {
        return Collections.unmodifiableList(radioNavigationAids);
    }

    public List<Route> getAtsLowerRoutes() {
        return Collections.unmodifiableList(atsLowerRoutes);
    }

    public List<Route> getRnavRoutes() {
        return Collections.unmodifiableList(rnavRoutes);
    }

    public List<Map<String, Object>> getAirportsForApi() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, double[]> e : airportPositions.entrySet()) {
            result.add(Map.of(
                    "icaoCode", e.getKey(),
                    "latitude", e.getValue()[0],
                    "longitude", e.getValue()[1]));
        }
        return result;
    }

    public Optional<double[]> findAirportPositionByIcao(String icaoCode) {
        if (icaoCode == null || icaoCode.isBlank()) {
            return Optional.empty();
        }
        double[] pos = airportPositions.get(icaoCode.toUpperCase().trim());
        return pos != null ? Optional.of(pos.clone()) : Optional.empty();
    }

    /**
     * Resolves IATA code from ICAO. Falls back to ICAO when not in airports.json or iataCode is null.
     */
    public String findIataByIcao(String icaoCode) {
        if (icaoCode == null || icaoCode.isBlank()) {
            return icaoCode != null ? icaoCode : "";
        }
        String key = icaoCode.toUpperCase().trim();
        return icaoToIata.getOrDefault(key, key);
    }

    private record AirportData(Map<String, double[]> positions, Map<String, String> icaoToIata) {
    }

    private AirportData loadAirportData() throws IOException {
        try (InputStream is = new ClassPathResource("fix/airports.json").getInputStream()) {
            JsonNode root = OBJECT_MAPPER.readTree(is);
            JsonNode airportsNode = root.get("airports");
            Map<String, double[]> positions = new HashMap<>();
            Map<String, String> icaoToIata = new HashMap<>();
            if (airportsNode != null && airportsNode.isArray()) {
                for (JsonNode a : airportsNode) {
                    String icao = a.get("icaoCode").asText().toUpperCase();
                    double lat = a.get("latitude").asDouble();
                    double lon = a.get("longitude").asDouble();
                    positions.put(icao, new double[] { lat, lon });
                    JsonNode iataNode = a.get("iataCode");
                    if (iataNode != null && !iataNode.isNull() && !iataNode.asText().isBlank()) {
                        icaoToIata.put(icao, iataNode.asText().toUpperCase());
                    }
                }
            }
            return new AirportData(positions, icaoToIata);
        }
    }

    private List<Waypoint> loadWaypoints() throws IOException {
        return loadWaypointsFromResource("fix/waypoints.json");
    }

    private List<Waypoint> loadWaypointsFromResource(String resourcePath) throws IOException {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode waypointNode = OBJECT_MAPPER.readTree(is);
            List<Waypoint> result = new ArrayList<>();
            for (JsonNode pointNode : waypointNode) {
                String pointName = pointNode.get("name").asText();
                double latitude = pointNode.get("latitude").asDouble();
                double longitude = pointNode.get("longitude").asDouble();
                String type = pointNode.get("type").asText();
                result.add(new Waypoint(pointName, latitude, longitude, type));
            }
            return result;
        }
    }

    private List<RadioNavigationAid> loadRadioNavigationAids() throws IOException {
        try (InputStream is = new ClassPathResource("fix/radio_navigation_aids.json").getInputStream()) {
            JsonNode aidNode = OBJECT_MAPPER.readTree(is);
            List<RadioNavigationAid> result = new ArrayList<>();
            for (JsonNode pointNode : aidNode) {
                String name = pointNode.get("name").asText();
                String id = pointNode.get("id").asText();
                String type = pointNode.get("type").asText();
                String frequency = pointNode.get("frequency").asText();
                double latitude = pointNode.get("latitude").asDouble();
                double longitude = pointNode.get("longitude").asDouble();
                result.add(new RadioNavigationAid(name, type, latitude, longitude));
            }
            return result;
        }
    }

    private List<Route> loadAtsLowerRoutes() throws IOException {
        return loadRoutesFromResource("fix/ats_lower_routes.json");
    }

    private List<Route> loadRnavRoutes() throws IOException {
        return loadRoutesFromResource("fix/rnav_routes.json");
    }

    private List<Route> loadRoutesFromResource(String resourcePath) throws IOException {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode routesNode = OBJECT_MAPPER.readTree(is).get("routes");
            List<Route> result = new ArrayList<>();
            for (JsonNode routeNode : routesNode) {
                String routeName = routeNode.get("name").asText();
                String routeDescription = routeNode.get("description").asText();
                List<RoutePoint> routePoints = new ArrayList<>();
                JsonNode pointsNode = routeNode.get("points");
                for (JsonNode pointNode : pointsNode) {
                    String pointName = pointNode.get("name").asText();
                    double latitude = pointNode.get("latitude").asDouble();
                    double longitude = pointNode.get("longitude").asDouble();
                    String type = pointNode.get("type").asText();
                    routePoints.add(new RoutePoint(pointName, latitude, longitude, type));
                }
                result.add(new Route(routeName, routeDescription, routePoints));
            }
            return result;
        }
    }

    @Override
    public Optional<FixPosition> findFixPositionByName(String fixName) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getName().equals(fixName)) {
                return Optional.of(new FixPosition(waypoint.getLatitude(), waypoint.getLongitude()));
            }
        }
        for (RadioNavigationAid aid : radioNavigationAids) {
            if (aid.getName().equals(fixName)) {
                return Optional.of(new FixPosition(aid.getLatitude(), aid.getLongitude()));
            }
        }
        return Optional.empty();
    }

    public String getRouteInfo() {
        return String.format("{\"waypoints\":%s, \"radioNavigationAids\":%s, \"atsLowerRoutes\":%s, \"rnavRoutes\":%s}",
                waypoints, radioNavigationAids, atsLowerRoutes, rnavRoutes);
    }
}
