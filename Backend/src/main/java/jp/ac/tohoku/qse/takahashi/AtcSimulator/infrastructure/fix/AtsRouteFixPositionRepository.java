package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.FixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RadioNavigationAid;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Route;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RoutePoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Waypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtsRouteFixPositionRepository implements FixPositionRepository {

    private final List<Waypoint> waypoints;
    private final List<RadioNavigationAid> radioNavigationAids;
    private final List<Route> atsLowerRoutes;
    private final List<Route> rnavRoutes;

    public AtsRouteFixPositionRepository() {
        try {
            this.waypoints = loadWaypoints();
            this.radioNavigationAids = loadRadioNavigationAids();
            this.atsLowerRoutes = loadAtsLowerRoutes();
            this.rnavRoutes = loadRnavRoutes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load fix data from classpath", e);
        }
    }

    private List<Waypoint> loadWaypoints() throws IOException {
        return loadWaypointsFromResource("fix/waypoints.json");
    }

    private List<Waypoint> loadWaypointsFromResource(String resourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode waypointNode = mapper.readTree(is);
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
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource("fix/radio_navigation_aids.json").getInputStream()) {
            JsonNode aidNode = mapper.readTree(is);
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
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode routesNode = mapper.readTree(is).get("routes");
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
