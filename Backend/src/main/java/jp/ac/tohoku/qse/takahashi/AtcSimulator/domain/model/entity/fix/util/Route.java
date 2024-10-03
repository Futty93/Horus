package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util;

import java.util.List;

public class Route {
    private String name;
    private String description;
    private List<RoutePoint> points;

    public Route(String name, String description, List<RoutePoint> points) {
        this.name = name;
        this.description = description;
        this.points = points;
    }
}
