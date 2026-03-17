export const NM_TO_KM = 1.852;

export function nmToKm(nm: number): number {
  return nm * NM_TO_KM;
}

export function nmToCanvasRadiusPx(
  nm: number,
  canvasWidth: number,
  rangeKm: number
): number {
  const km = nmToKm(nm);
  const pixelsPerKm = canvasWidth / rangeKm;
  return km * pixelsPerKm;
}
