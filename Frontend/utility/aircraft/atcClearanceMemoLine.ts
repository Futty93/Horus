/** 実測が管制メモの当該軸と「一致」とみなす閾値（spec 20260326、単体テストで固定）。 */
export const ATC_ALTITUDE_TOLERANCE_FT = 200;
export const ATC_HEADING_TOLERANCE_DEG = 5;
export const ATC_GROUND_SPEED_TOLERANCE_KT = 10;

export type InstructedVectorLike = {
  heading: number;
  groundSpeed: number;
  altitude: number;
};

export function smallestHeadingDifferenceDeg(a: number, b: number): number {
  const d = Math.abs(a - b) % 360;
  return Math.min(d, 360 - d);
}

function flHundreds(ft: number): string {
  return String(Math.floor(ft / 100)).padStart(3, "0");
}

function hdg3(deg: number): string {
  let n = Math.round(deg) % 360;
  if (n < 0) n += 360;
  return String(n).padStart(3, "0");
}

function spdInt(kt: number): string {
  return String(Math.round(kt));
}

function altitudeMatches(clearFt: number, actualFt: number): boolean {
  return Math.abs(clearFt - actualFt) <= ATC_ALTITUDE_TOLERANCE_FT;
}

function headingMatches(clearDeg: number, actualDeg: number): boolean {
  return (
    smallestHeadingDifferenceDeg(clearDeg, actualDeg) <=
    ATC_HEADING_TOLERANCE_DEG
  );
}

function speedMatches(clearKt: number, actualKt: number): boolean {
  return Math.abs(clearKt - actualKt) <= ATC_GROUND_SPEED_TOLERANCE_KT;
}

/** 入力欄の初期値 0 は「未記録」とみなし、その軸の差分は出さない（北は 360° で記録）。 */
const MIN_CLEARANCE_ALTITUDE_FT = 100;

export function isAltitudeClearanceRecorded(c: InstructedVectorLike): boolean {
  return c.altitude >= MIN_CLEARANCE_ALTITUDE_FT;
}

function isHeadingClearanceRecorded(c: InstructedVectorLike): boolean {
  return c.heading > 0;
}

function isGroundSpeedClearanceRecorded(c: InstructedVectorLike): boolean {
  return c.groundSpeed > 0;
}

export type FormatAtcClearanceMemoLineOptions = {
  /**
   * Controller 画面で高度行が既に「クリアランス vs 実測」として描画されているとき、
   * 水色行に高度ペアを重ねない。
   */
  omitAltitude?: boolean;
};

/**
 * 管制クリアランス vs 実測。遵守済みの軸は出さない。略号・スラッシュ区切り（spec データブロック寄せ）。
 * 記録されていない軸（初期値 0）は比較に含めない。
 */
export function formatAtcClearanceMemoLine(
  clearance: InstructedVectorLike | null | undefined,
  actual: { altitudeFt: number; heading: number; groundSpeed: number },
  options?: FormatAtcClearanceMemoLineOptions
): string | null {
  if (!clearance) return null;

  const parts: string[] = [];
  const omitAltitude = options?.omitAltitude === true;

  if (
    !omitAltitude &&
    isAltitudeClearanceRecorded(clearance) &&
    !altitudeMatches(clearance.altitude, actual.altitudeFt)
  ) {
    parts.push(
      `${flHundreds(clearance.altitude)}/${flHundreds(actual.altitudeFt)}`
    );
  }
  if (
    isHeadingClearanceRecorded(clearance) &&
    !headingMatches(clearance.heading, actual.heading)
  ) {
    parts.push(`${hdg3(clearance.heading)}/${hdg3(actual.heading)}`);
  }
  if (
    isGroundSpeedClearanceRecorded(clearance) &&
    !speedMatches(clearance.groundSpeed, actual.groundSpeed)
  ) {
    parts.push(
      `${spdInt(clearance.groundSpeed)}/${spdInt(actual.groundSpeed)}`
    );
  }

  return parts.length > 0 ? parts.join(" ") : null;
}
