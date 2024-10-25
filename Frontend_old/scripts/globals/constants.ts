interface GlobalConstants {
  EARTH_RADIUS_KM: number;
  CANVAS_BACKGROUND_COLOR: string;
  WAYPOINT_COLOR: string;
  NAV_AID_COLOR: string;
  LINE_COLOR: string;
  FONT_STYLE_IN_CANVAS: string;

  LOCATION_UPDATE_INTERVAL: number; // 更新間隔（ms）
  REFRESH_RATE: number;
}

export const GLOBAL_CONSTANTS: GlobalConstants = {
  EARTH_RADIUS_KM: 6378.1,
  CANVAS_BACKGROUND_COLOR: '#FFFFFF',
  WAYPOINT_COLOR: '#00f',
  NAV_AID_COLOR: '#f00',
  LINE_COLOR: '#000',
  FONT_STYLE_IN_CANVAS: '12px Arial',

  LOCATION_UPDATE_INTERVAL: 1000, // 更新間隔（ms）
  REFRESH_RATE: 60,
};