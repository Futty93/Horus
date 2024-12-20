// グローバル設定の型を定義
interface GlobalSettings {
  canvasWidth: number;
  canvasHeight: number;
  displayRange: number;
  centerCoordinates: { latitude: number; longitude: number }; // 座標はオブジェクトとして定義
  isDisplaying: DisplaySettings;
  callsignExtractionStatus: string;
}

// 表示に関する設定を別途分けて定義
interface DisplaySettings {
  waypointName: boolean;
  waypointPoint: boolean;
  radioNavigationAidsName: boolean;
  radioNavigationAidsPoint: boolean;
  atsLowerRoute: boolean;
  rnavRoute: boolean;
}

// グローバル設定の初期値
export const GLOBAL_SETTINGS: GlobalSettings = {
  canvasWidth: 2000,
  canvasHeight: 2000,
  displayRange: 400,
  centerCoordinates: { latitude: 35.6895, longitude: 139.6917 }, // 東京の座標
  isDisplaying: {
    waypointName: false,
    waypointPoint: true,
    radioNavigationAidsName: false,
    radioNavigationAidsPoint: true,
    atsLowerRoute: false,
    rnavRoute: true,
  },
  callsignExtractionStatus: 'NO_VALUE',
};