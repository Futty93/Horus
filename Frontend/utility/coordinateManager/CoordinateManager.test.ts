import { CoordinateManager } from "./CoordinateManager";
import { GLOBAL_SETTINGS } from "../globals/settings";

const centerCoordinate = { latitude: 35.0, longitude: 139.0 };
const displayRange = { range: 400 };

beforeEach(() => {
  GLOBAL_SETTINGS.canvasWidth = 1000;
  GLOBAL_SETTINGS.canvasHeight = 1000;
});

describe("CoordinateManager", () => {
  describe("calculateCanvasCoordinates", () => {
    it("returns center of canvas when point equals center", () => {
      const result = CoordinateManager.calculateCanvasCoordinates(
        centerCoordinate,
        centerCoordinate,
        displayRange
      );

      expect(result.x).toBeCloseTo(500, 0);
      expect(result.y).toBeCloseTo(500, 0);
    });

    it("returns different coordinates for different lat/lon", () => {
      const pointNorth = { latitude: 36.0, longitude: 139.0 };
      const result = CoordinateManager.calculateCanvasCoordinates(
        pointNorth,
        centerCoordinate,
        displayRange
      );

      expect(result.y).toBeLessThan(500);
      expect(result.x).toBeCloseTo(500, -1);
    });
  });

  describe("calculateGeoCoordinates", () => {
    it("returns center coordinate when canvas position is center", () => {
      const result = CoordinateManager.calculateGeoCoordinates(
        500,
        500,
        centerCoordinate,
        displayRange
      );

      expect(result.latitude).toBeCloseTo(centerCoordinate.latitude, 2);
      expect(result.longitude).toBeCloseTo(centerCoordinate.longitude, 2);
    });

    it("round-trips with calculateCanvasCoordinates", () => {
      const point = { latitude: 35.5, longitude: 139.5 };
      const canvas = CoordinateManager.calculateCanvasCoordinates(
        point,
        centerCoordinate,
        displayRange
      );
      const geo = CoordinateManager.calculateGeoCoordinates(
        canvas.x,
        canvas.y,
        centerCoordinate,
        displayRange
      );

      expect(geo.latitude).toBeCloseTo(point.latitude, 1);
      expect(geo.longitude).toBeCloseTo(point.longitude, 1);
    });
  });
});
