import { Coordinate } from "@/context/centerCoordinateContext";
import { GLOBAL_CONSTANTS } from "../globals/constants";
import { GLOBAL_SETTINGS } from "../globals/settings";
import { DisplayRange } from "@/context/displayRangeContext";

// 地球の丸みを考慮にいれ、緯度・経度をキャンバス上の座標に変換するクラス
export class CoordinateManager {

  /**
   * 緯度・経度をキャンバス上の座標に変換します。
   * @param centerLatitude 中心の緯度
   * @param centerLongitude 中心の経度
   * @param range 表示領域の1辺の長さ（キロメートル）
   * @param targetLatitude 対象の緯度
   * @param targetLongitude 対象の経度
   * @returns キャンバス上の座標 {x, y}
   */
  public static calculateCanvasCoordinates(pointCoordinate: Coordinate, centerCoordinate: Coordinate, displayRange: DisplayRange): { x: number, y: number } {
    // Convert degrees to radians
    const toRadians = (degrees: number) => degrees * (Math.PI / 180);

    // Haversine formula to calculate the distance between two points on the Earth
    const deltaLat = toRadians(pointCoordinate.latitude - centerCoordinate.latitude);
    const deltaLon = toRadians(pointCoordinate.longitude - centerCoordinate.longitude);
    const a = Math.sin(deltaLat / 2) ** 2 + Math.cos(toRadians(centerCoordinate.latitude)) * Math.cos(toRadians(pointCoordinate.latitude)) * Math.sin(deltaLon / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distanceKm = GLOBAL_CONSTANTS.EARTH_RADIUS_KM * c;

    // Calculate the bearing from the center to the point
    const y = Math.sin(deltaLon) * Math.cos(toRadians(pointCoordinate.latitude));
    const x = Math.cos(toRadians(centerCoordinate.latitude)) * Math.sin(toRadians(pointCoordinate.latitude)) - Math.sin(toRadians(centerCoordinate.latitude)) * Math.cos(toRadians(pointCoordinate.latitude)) * Math.cos(deltaLon);
    const bearing = Math.atan2(y, x);

    // Scale distance to canvas pixels
    // const pixelsPerKm = range / this.canvasWidth;
    const pixelsPerKm = GLOBAL_SETTINGS.canvasWidth / displayRange.range;
    const distancePx = distanceKm * pixelsPerKm;

    // Calculate canvas coordinates
    const canvasX = (GLOBAL_SETTINGS.canvasWidth / 2) + distancePx * Math.sin(bearing);
    const canvasY = (GLOBAL_SETTINGS.canvasHeight / 2) - distancePx * Math.cos(bearing);

    return { x: canvasX, y: canvasY };
  }

  /**
   * キャンバス上の座標を緯度・経度に変換します。
   * @param canvasX キャンバス上のX座標
   * @param canvasY キャンバス上のY座標
   * @param centerCoordinate 中心の緯度・経度
   * @param displayRange 表示領域の1辺の長さ（キロメートル）
   * @returns 緯度・経度 {latitude, longitude}
   */
  public static calculateGeoCoordinates(canvasX: number, canvasY: number, centerCoordinate: Coordinate, displayRange: DisplayRange): { latitude: number, longitude: number } {
    // Convert degrees to radians
    const toRadians = (degrees: number) => degrees * (Math.PI / 180);
    // Convert radians to degrees
    const toDegrees = (radians: number) => radians * (180 / Math.PI);

    // Calculate the distance from the center of the canvas
    const deltaX = canvasX - (GLOBAL_SETTINGS.canvasWidth / 2);
    const deltaY = (GLOBAL_SETTINGS.canvasHeight / 2) - canvasY;
    const distancePx = Math.sqrt(deltaX ** 2 + deltaY ** 2);

    // Scale pixels to kilometers
    const pixelsPerKm = GLOBAL_SETTINGS.canvasWidth / displayRange.range;
    const distanceKm = distancePx / pixelsPerKm;

    // Calculate the bearing from the center point to the canvas point
    const bearing = Math.atan2(deltaX, deltaY);

    // Haversine formula to calculate new latitude and longitude
    const angularDistance = distanceKm / GLOBAL_CONSTANTS.EARTH_RADIUS_KM;

    const newLatitude = Math.asin(
        Math.sin(toRadians(centerCoordinate.latitude)) * Math.cos(angularDistance) +
        Math.cos(toRadians(centerCoordinate.latitude)) * Math.sin(angularDistance) * Math.cos(bearing)
    );

    const newLongitude = toRadians(centerCoordinate.longitude) + Math.atan2(
        Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(toRadians(centerCoordinate.latitude)),
        Math.cos(angularDistance) - Math.sin(toRadians(centerCoordinate.latitude)) * Math.sin(newLatitude)
    );

    return {
        latitude: toDegrees(newLatitude),
        longitude: toDegrees(newLongitude)
    };
  }

  /**
   * 度をラジアンに変換します。
   * @param deg 度
   * @returns ラジアン
   */
  private degToRad(deg: number): number {
      return deg * (Math.PI / 180);
  }

  /**
   * 2つの座標間の距離を計算します。
   * @param lat1 座標1の緯度
   * @param lon1 座標1の経度
   * @param lat2 座標2の緯度
   * @param lon2 座標2の経度
   * @returns 距離（キロメートル）
   */
  public calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
      const dLat = this.degToRad(lat2 - lat1);
      const dLon = this.degToRad(lon2 - lon1);
      const a =
          Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.cos(this.degToRad(lat1)) * Math.cos(this.degToRad(lat2)) *
          Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      const distance = GLOBAL_CONSTANTS.EARTH_RADIUS_KM * c;
      return distance;
  }

/**
 * Calculates the canvas position of an aircraft 1 minute into the future based on its current speed and heading.
 * @param speed - Current speed of the aircraft in knots.
 * @param heading - Current heading of the aircraft in degrees.
 * @param canvasWidth - Width of the canvas in pixels.
 * @param canvasHeight - Height of the canvas in pixels.
 * @param displayRange - The width of the displayed airspace in kilometers.
 * @param currentPosition - Current position of the aircraft on the canvas in pixels.
 * @returns The position of the aircraft on the canvas after 1 minute.
 */
public static calculateFuturePositionOnCanvas(
    speed: number,
    heading: number,
    canvasWidth: number,
    canvasHeight: number,
    displayRange: number,
    currentPosition: { x: number; y: number }
  ): { futureX: number; futureY: number } {
    // Convert speed from knots to kilometers per minute
    const speedKmPerMin = speed * 1.852 / 60;
  
    // Convert heading from degrees to radians
    const headingRad = (heading - 90) * (Math.PI / 180);
  
    // Calculate the distance traveled in x and y directions
    const deltaX = speedKmPerMin * Math.cos(headingRad);
    const deltaY = - speedKmPerMin * Math.sin(headingRad);
  
    // Calculate the new position in kilometers
    const futurePositionX = currentPosition.x + deltaX;
    const futurePositionY = currentPosition.y + deltaY;
  
    // Convert the future position from kilometers to canvas pixels
    const kmPerPixel = displayRange / canvasWidth;
    const futureX = currentPosition.x + (futurePositionX - currentPosition.x) / kmPerPixel;
    const futureY = currentPosition.y - (futurePositionY - currentPosition.y) / kmPerPixel;
  
    return {
      futureX: Math.min(Math.max(futureX, 0), canvasWidth),
      futureY: Math.min(Math.max(futureY, 0), canvasHeight)
    };
  }
}

export default CoordinateManager;
