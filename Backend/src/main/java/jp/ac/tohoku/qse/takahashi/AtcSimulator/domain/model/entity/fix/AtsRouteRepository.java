package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.AtsRouteData;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Fix;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Route;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtsRouteRepository {
    public static void main(String[] args) throws IOException {
        // JSONファイルパス
        String radioNavPath = "./radio_navigation_aids.json";
        String waypointPath = "./waypoint.json";
        String atsRoutesPath = "./ats_lower_routes.json";

        // JSONファイルを読み込む
        ObjectMapper mapper = new ObjectMapper();

        // 無線航法装置データを読み込む
        List<Map<String, Object>> radioNavs = mapper.readValue(new File(radioNavPath), new TypeReference<>(){});
        List<Fix> radioNavPoints = new ArrayList<>();
        for (Map<String, Object> radioNav : radioNavs) {
            Fix fix = new Fix();
            fix.name = (String) radioNav.get("name");
            fix.type = "RADIO_NAVIGATION_AID";
            fix.latitude = (double) radioNav.get("latitude");
            fix.longitude = (double) radioNav.get("longitude");
            radioNavPoints.add(fix);
        }

        // ウェイポイントデータを読み込む
        List<Fix> waypoints = mapper.readValue(new File(waypointPath), new TypeReference<>(){});
        for (Fix waypoint : waypoints) {
            waypoint.type = "WAYPOINT";
        }

        // ATSルートデータを読み込む
        List<Route> atsRoutes = mapper.readValue(new File(atsRoutesPath), new TypeReference<>(){});

        // ポイント名から座標を引くためのマップを作成
        Map<String, Fix> pointMap = new HashMap<>();
        for (Fix point : radioNavPoints) {
            pointMap.put(point.name, point);
        }
        for (Fix waypoint : waypoints) {
            pointMap.put(waypoint.name, waypoint);
        }

        // ルートごとの接続情報を作成
        List<List<String>> connections = new ArrayList<>();
        for (Route route : atsRoutes) {
            List<String> connection = new ArrayList<>();
            for (String pointName : route.fixes) {
                if (pointMap.containsKey(pointName)) {
                    connection.add(pointName);
                }
            }
            connections.add(connection);
        }

        // 結合データを作成
        AtsRouteData atsRouteData = new AtsRouteData();
        atsRouteData.fixes = new ArrayList<>(pointMap.values());
        atsRouteData.connections = connections;

        // 結果を表示するかファイルに保存
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/combined_data.json"), atsRouteData);

        System.out.println("Data combined and written to combined_data.json");
    }
}
