import { Waypoint } from './AtsRouteManager/RouteInterfaces/Waypoint';

export class WaypointManager {
  private waypoints: Waypoint[] = [];
  private filteredWaypoints: Waypoint[] = [];
  static loadWaypoints: any;

  /**
   * 指定された中心座標と表示範囲に基づいてウェイポイントをフィルタリングします。
   * @param center 中心座標
   * @param range 表示範囲
   */
  public　updateFilteredWaypoints(center: { latitude: number, longitude: number }, range: number): void {
    // console.log(range)
    this.filteredWaypoints = this.waypoints.filter(waypoint => {
        return this.isWithinRange(center, range, waypoint);
    });
  }

  /**
   * ウェイポイントが指定された範囲内にあるかどうかを判定します。
   * @param center 中心座標
   * @param range 表示範囲
   * @param waypoint 判定するウェイポイント
   * @returns ウェイポイントが範囲内にある場合はtrue、それ以外はfalse
   */
  private isWithinRange(center: { latitude: number, longitude: number }, range: number, waypoint: Waypoint): boolean {
      const distance = this.calculateDistance(center.latitude, center.longitude, waypoint.latitude, waypoint.longitude);
      return distance <= range;
  }

  /**
   * 2つの座標間の距離を計算します。
   * @param lat1 座標1の緯度
   * @param lon1 座標1の経度
   * @param lat2 座標2の緯度
   * @param lon2 座標2の経度
   * @returns 距離（キロメートル）
   */
  private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
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
   * 度をラジアンに変換します。
   * @param deg 度
   * @returns ラジアン
   */
  private degToRad(deg: number): number {
      return deg * (Math.PI / 180);
  }

  /**
   * フィルタリングされたウェイポイントの配列を返します。
   * @returns フィルタリングされたウェイポイントの配列
   */
  public getFilteredWaypoints(): Waypoint[] {
    return this.filteredWaypoints.map(waypoint => ({ ...waypoint }));
  }
}

export default WaypointManager;
