package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RadioNavigationAid;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Route;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RoutePoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Waypoint;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AtsRouteRepository {
    private static AtsRouteRepository instance;

    private List<Waypoint> waypoints;
    private List<RadioNavigationAid> radioNavigationAids;
    private List<Route> atsLowerRoutes;
    private List<Route> rnavRoutes;

    private AtsRouteRepository() throws IOException {
        waypoints = loadWaypoints();
        this.radioNavigationAids = loadRadioNavigationAids();
        this.atsLowerRoutes = loadAtsLowerRoutes();
        this.rnavRoutes = loadRnavRoutes();
    }

    public static synchronized AtsRouteRepository getInstance() throws IOException {
        if (instance == null) {
            instance = new AtsRouteRepository();
        }
        return instance;
    }

    public static void main(String[] args) throws IOException {
        AtsRouteRepository.getInstance();
    }

    // Private methods to load data from JSON files
    private List<Waypoint> loadWaypoints() throws IOException {
        String AtsLowerRoutesPath = "src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/domain/model/entity/fix/JsonFiles/waypoints.json";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode waypointNode = mapper.readTree(new File(AtsLowerRoutesPath));

        List<Waypoint> waypoints = new ArrayList<>();

        for (JsonNode pointNode : waypointNode) {
            String pointName = pointNode.get("name").asText();
            double latitude = pointNode.get("latitude").asDouble();
            double longitude = pointNode.get("longitude").asDouble();

            Waypoint waypoint = new Waypoint(pointName, latitude, longitude);
            waypoints.add(waypoint);
        }

        return waypoints;
    }

    private List<RadioNavigationAid> loadRadioNavigationAids() throws IOException {
        String AtsLowerRoutesPath = "src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/domain/model/entity/fix/JsonFiles/radio_navigation_aids.json";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode aidNode = mapper.readTree(new File(AtsLowerRoutesPath));

        List<RadioNavigationAid> radioNavigationAids = new ArrayList<>();

        for (JsonNode pointNode : aidNode) {
            String name = pointNode.get("name").asText();
            String id = pointNode.get("id").asText();
            String type = pointNode.get("type").asText();
            String frequency = pointNode.get("frequency").asText();
            double latitude = pointNode.get("latitude").asDouble();
            double longitude = pointNode.get("longitude").asDouble();

            RadioNavigationAid radioNavigationAid = new RadioNavigationAid(name, id, type, frequency, latitude, longitude);
            radioNavigationAids.add(radioNavigationAid);
        }

        return radioNavigationAids;
    }

    private List<Route> loadAtsLowerRoutes() throws IOException {
        String AtsLowerRoutesPath = "src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/domain/model/entity/fix/JsonFiles/ats_lower_routes.json";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode routesNode = mapper.readTree(new File(AtsLowerRoutesPath)).get("routes");

        List<Route> atsLowerRoutes = new ArrayList<>();

        // Iterate over each route in the routes array
        for (JsonNode routeNode : routesNode) {
            // Extract route name and description
            String routeName = routeNode.get("name").asText();
            String routeDescription = routeNode.get("description").asText();

            // Extract the list of points (waypoints) for this route
            List<RoutePoint> routePoints = new ArrayList<>();
            JsonNode pointsNode = routeNode.get("points");

            for (JsonNode pointNode : pointsNode) {
                String pointName = pointNode.get("name").asText();
                double latitude = pointNode.get("latitude").asDouble();
                double longitude = pointNode.get("longitude").asDouble();
                String type = pointNode.get("type").asText();  // e.g., "waypoint"

                // Create a Waypoint object (or FixBase if needed)
                RoutePoint routePoint = new RoutePoint(pointName, latitude, longitude, type);
                routePoints.add(routePoint);
            }

            // Create a Route object and add it to the list
            Route route = new Route(routeName, routeDescription, routePoints);
            atsLowerRoutes.add(route);
        }

        return atsLowerRoutes;
    }

    private List<Route> loadRnavRoutes() throws IOException {
        String AtsLowerRoutesPath = "src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/domain/model/entity/fix/JsonFiles/rnav_routes.json";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode routesNode = mapper.readTree(new File(AtsLowerRoutesPath)).get("routes");

        List<Route> rnavRoutes = new ArrayList<>();

        // Iterate over each route in the routes array
        for (JsonNode routeNode : routesNode) {
            // Extract route name and description
            String routeName = routeNode.get("name").asText();
            String routeDescription = routeNode.get("description").asText();

            // Extract the list of points (waypoints) for this route
            List<RoutePoint> routePoints = new ArrayList<>();
            JsonNode pointsNode = routeNode.get("points");

            for (JsonNode pointNode : pointsNode) {
                String pointName = pointNode.get("name").asText();
                double latitude = pointNode.get("latitude").asDouble();
                double longitude = pointNode.get("longitude").asDouble();
                String type = pointNode.get("type").asText();  // e.g., "waypoint"

                // Create a Waypoint object (or FixBase if needed)
                RoutePoint routePoint = new RoutePoint(pointName, latitude, longitude, type);
                routePoints.add(routePoint);
            }

            // Create a Route object and add it to the list
            Route route = new Route(routeName, routeDescription, routePoints);
            rnavRoutes.add(route);
        }

        return rnavRoutes;
    }

    @Override
    public String toString() {
        return String.format("{\"waypoints\":%s, \"radioNavigationAids\":%s, \"atsLowerRoutes\":%s, \"rnavRoutes\":%s}", waypoints, radioNavigationAids, atsLowerRoutes, rnavRoutes);
    }
}
