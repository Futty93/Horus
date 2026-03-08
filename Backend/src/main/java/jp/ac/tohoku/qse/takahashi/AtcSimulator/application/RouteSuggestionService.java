package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RadioNavigationAid;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Route;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RoutePoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Waypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;

public class RouteSuggestionService {

    private static final double MAX_NEAREST_FIX_DISTANCE_KM = 800.0;

    private final AtsRouteFixPositionRepository repository;
    private final Map<String, double[]> fixPositions;
    private final Map<String, Set<String>> graph;

    public RouteSuggestionService(AtsRouteFixPositionRepository repository) {
        this.repository = repository;
        this.fixPositions = buildFixPositions();
        this.graph = buildGraph();
    }

    public static final String REASON_REJECT = "REJECT";
    public static final String REASON_NO_AIRPORT = "NO_AIRPORT";
    public static final String REASON_NO_NEAREST_FIX = "NO_NEAREST_FIX";
    public static final String REASON_SAME_FIX = "SAME_FIX";
    public static final String REASON_OK_ASTAR = "OK_ASTAR";
    public static final String REASON_FALLBACK = "FALLBACK_NO_PATH";

    public SuggestResult suggestRouteWithReason(String originIcao, String destinationIcao) {
        if (originIcao == null || originIcao.isBlank() || destinationIcao == null || destinationIcao.isBlank()) {
            return new SuggestResult(List.of(), REASON_REJECT);
        }

        Optional<double[]> originPos = repository.findAirportPositionByIcao(originIcao);
        Optional<double[]> destPos = repository.findAirportPositionByIcao(destinationIcao);
        if (originPos.isEmpty() || destPos.isEmpty()) {
            return new SuggestResult(List.of(), REASON_NO_AIRPORT);
        }

        double[] o = originPos.get();
        double[] d = destPos.get();
        Optional<String> startFix = findNearestFix(o[0], o[1]);
        Optional<String> goalFix = findNearestFix(d[0], d[1]);
        if (startFix.isEmpty() || goalFix.isEmpty()) {
            return new SuggestResult(List.of(), REASON_NO_NEAREST_FIX);
        }
        if (startFix.get().equals(goalFix.get())) {
            return new SuggestResult(List.of(startFix.get()), REASON_SAME_FIX);
        }

        List<String> path = aStar(startFix.get(), goalFix.get());
        if (path != null && !path.isEmpty()) {
            return new SuggestResult(path, REASON_OK_ASTAR);
        }
        return new SuggestResult(List.of(startFix.get(), goalFix.get()), REASON_FALLBACK);
    }

    public List<String> suggestRoute(String originIcao, String destinationIcao) {
        return suggestRouteWithReason(originIcao, destinationIcao).waypoints();
    }

    public record SuggestResult(List<String> waypoints, String reason) {}

    private Map<String, double[]> buildFixPositions() {
        Map<String, double[]> result = new HashMap<>();
        for (Waypoint w : repository.getWaypoints()) {
            result.put(w.getName(), new double[] { w.getLatitude().toDouble(), w.getLongitude().toDouble() });
        }
        for (RadioNavigationAid r : repository.getRadioNavigationAids()) {
            result.put(r.getName(), new double[] { r.getLatitude().toDouble(), r.getLongitude().toDouble() });
        }
        return result;
    }

    private Map<String, Set<String>> buildGraph() {
        Map<String, Set<String>> adj = new HashMap<>();
        for (Route route : repository.getAtsLowerRoutes()) {
            addRouteEdges(adj, route);
        }
        for (Route route : repository.getRnavRoutes()) {
            addRouteEdges(adj, route);
        }
        return adj;
    }

    private void addRouteEdges(Map<String, Set<String>> adj, Route route) {
        List<RoutePoint> points = route.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            String a = points.get(i).getName();
            String b = points.get(i + 1).getName();
            if (!fixPositions.containsKey(a) || !fixPositions.containsKey(b)) {
                continue;
            }
            adj.computeIfAbsent(a, k -> new HashSet<>()).add(b);
            adj.computeIfAbsent(b, k -> new HashSet<>()).add(a);
        }
    }

    private Optional<String> findNearestFix(double lat, double lon) {
        String nearest = null;
        double minDist = MAX_NEAREST_FIX_DISTANCE_KM;
        for (Map.Entry<String, double[]> e : fixPositions.entrySet()) {
            double d = PositionUtils.calculateApproximateDistance(lat, lon, e.getValue()[0], e.getValue()[1]);
            if (d < minDist) {
                minDist = d;
                nearest = e.getKey();
            }
        }
        return Optional.ofNullable(nearest);
    }

    private List<String> aStar(String start, String goal) {
        if (!graph.containsKey(start) || !graph.containsKey(goal)) {
            return null;
        }

        Map<String, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);
        Map<String, String> cameFrom = new HashMap<>();
        PriorityQueue<AStarNode> open = new PriorityQueue<>();
        open.offer(new AStarNode(start, 0.0, heuristic(start, goal)));
        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
            AStarNode current = open.poll();
            if (current.name.equals(goal)) {
                return reconstructPath(cameFrom, goal);
            }
            if (closed.contains(current.name)) {
                continue;
            }
            closed.add(current.name);

            Set<String> neighbors = graph.get(current.name);
            if (neighbors == null) {
                continue;
            }
            for (String neighbor : neighbors) {
                if (closed.contains(neighbor)) {
                    continue;
                }
                double[] currPos = fixPositions.get(current.name);
                double[] neighborPos = fixPositions.get(neighbor);
                double edgeCost = PositionUtils.calculateApproximateDistance(
                        currPos[0], currPos[1], neighborPos[0], neighborPos[1]);
                double tentativeG = gScore.getOrDefault(current.name, Double.POSITIVE_INFINITY) + edgeCost;

                if (tentativeG < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighbor, current.name);
                    gScore.put(neighbor, tentativeG);
                    open.offer(new AStarNode(neighbor, tentativeG, heuristic(neighbor, goal)));
                }
            }
        }
        return null;
    }

    private double heuristic(String from, String to) {
        double[] a = fixPositions.get(from);
        double[] b = fixPositions.get(to);
        if (a == null || b == null) {
            return 0;
        }
        return PositionUtils.calculateApproximateDistance(a[0], a[1], b[0], b[1]);
    }

    private List<String> reconstructPath(Map<String, String> cameFrom, String current) {
        List<String> path = new ArrayList<>();
        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }
        return path;
    }

    private static final class AStarNode implements Comparable<AStarNode> {
        final String name;
        final double fScore;

        AStarNode(String name, double gScore, double hScore) {
            this.name = name;
            this.fScore = gScore + hScore;
        }

        @Override
        public int compareTo(AStarNode o) {
            return Double.compare(fScore, o.fScore);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AStarNode that = (AStarNode) obj;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
