import { searchFixName } from "./FixNameSearch";
import type { Waypoint } from "./RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";

jest.mock("../coordinateManager/CoordinateManager", () => ({
  __esModule: true,
  default: {
    calculateGeoCoordinates: jest.fn(() => ({
      latitude: 35.51,
      longitude: 139.51,
    })),
  },
}));

const CoordinateManager =
  require("../coordinateManager/CoordinateManager").default;

describe("searchFixName", () => {
  const waypoints: Waypoint[] = [
    { name: "ABENO", latitude: 35.5, longitude: 139.5, type: "waypoint" },
    { name: "OMOTE", latitude: 36.0, longitude: 140.0, type: "waypoint" },
  ];
  const centerCoordinate = { latitude: 35.0, longitude: 139.0 };
  const displayRange = { range: 400 };

  it("returns findNearestFixName result using canvas-to-geo conversion", () => {
    (CoordinateManager.calculateGeoCoordinates as jest.Mock).mockReturnValue({
      latitude: 35.51,
      longitude: 139.51,
    });

    const result = searchFixName(
      waypoints,
      [],
      { x: 500, y: 500 },
      centerCoordinate,
      displayRange
    );

    expect(result).toBe("ABENO");
    expect(CoordinateManager.calculateGeoCoordinates).toHaveBeenCalledWith(
      500,
      500,
      centerCoordinate,
      displayRange
    );
  });
});
