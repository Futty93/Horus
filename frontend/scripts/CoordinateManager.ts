// 地球の丸みを考慮にいれ、緯度・経度をキャンバス上の座標に変換するクラス
export class CoordinateManager {
  private canvasWidth: number;
  private canvasHeight: number;

  constructor(canvasWidth: number, canvasHeight: number) {
      this.canvasWidth = canvasWidth;
      this.canvasHeight = canvasHeight;
  }

  /**
   * 緯度・経度をキャンバス上の座標に変換します。
   * @param centerLatitude 中心の緯度
   * @param centerLongitude 中心の経度
   * @param range 表示領域の1辺の長さ（キロメートル）
   * @param targetLatitude 対象の緯度
   * @param targetLongitude 対象の経度
   * @returns キャンバス上の座標 {x, y}
   */
  public calculateCanvasCoordinates(centerLat: number, centerLon: number, range: number, pointLat: number, pointLon: number): { x: number, y: number } {
    const EARTH_RADIUS_KM = 6371; 
    // Convert degrees to radians
    const toRadians = (degrees) => degrees * (Math.PI / 180);

    // Haversine formula to calculate the distance between two points on the Earth
    const deltaLat = toRadians(pointLat - centerLat);
    const deltaLon = toRadians(pointLon - centerLon);
    const a = Math.sin(deltaLat / 2) ** 2 + Math.cos(toRadians(centerLat)) * Math.cos(toRadians(pointLat)) * Math.sin(deltaLon / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distanceKm = EARTH_RADIUS_KM * c;

    // Calculate the bearing from the center to the point
    const y = Math.sin(deltaLon) * Math.cos(toRadians(pointLat));
    const x = Math.cos(toRadians(centerLat)) * Math.sin(toRadians(pointLat)) - Math.sin(toRadians(centerLat)) * Math.cos(toRadians(pointLat)) * Math.cos(deltaLon);
    const bearing = Math.atan2(y, x);

    // Scale distance to canvas pixels
    // const pixelsPerKm = range / this.canvasWidth;
    const pixelsPerKm = this.canvasWidth / range;
    const distancePx = distanceKm * pixelsPerKm;

    // Calculate canvas coordinates
    const canvasX = (this.canvasWidth / 2) + distancePx * Math.sin(bearing);
    const canvasY = (this.canvasHeight / 2) - distancePx * Math.cos(bearing);

    return { x: canvasX, y: canvasY };
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
      const R = 6371; // 地球の半径（キロメートル）
      const dLat = this.degToRad(lat2 - lat1);
      const dLon = this.degToRad(lon2 - lon1);
      const a =
          Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.cos(this.degToRad(lat1)) * Math.cos(this.degToRad(lat2)) *
          Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      const distance = R * c;
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
public calculateFuturePositionOnCanvas(
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
