/**
 * データブロック 2 行目: 目標高度 ft vs 実測 ft。
 * FL 百の位が同一なら矢印なし 1 値のみ（シミュレーションの細かい ft 差で 310↓310 になるのを防ぐ）。
 */
export function formatAltitudeTargetVsActualLabel(
  targetFt: number,
  actualFt: number
): string {
  const targetFl = Math.floor(targetFt / 100);
  const actualFl = Math.floor(actualFt / 100);
  if (targetFl === actualFl) {
    return actualFl.toString();
  }
  if (targetFt > actualFt) {
    return `${targetFl} ↑ ${actualFl}`;
  }
  return `${targetFl} ↓ ${actualFl}`;
}
