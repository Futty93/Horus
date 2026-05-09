/**
 * Matches backend `StringUtils.generatePairId`: lexicographically smaller callsign first,
 * joined with a single "-". Use prefix/suffix matching against `ownCallsign` so hyphenated
 * callsigns still parse (splitting on "-" alone would be wrong).
 */
export function parseOtherCallsignFromPairId(
  pairId: string,
  ownCallsign: string
): string | null {
  const prefix = `${ownCallsign}-`;
  if (pairId.startsWith(prefix)) {
    const other = pairId.slice(prefix.length);
    return other.length > 0 ? other : null;
  }
  const suffix = `-${ownCallsign}`;
  if (pairId.endsWith(suffix)) {
    const other = pairId.slice(0, -suffix.length);
    return other.length > 0 ? other : null;
  }
  return null;
}

/** Backend units: horizontal NM, vertical ft, TCPA seconds (RiskAssessment). */
export function formatConflictPairLine(alert: {
  closestHorizontalDistance: number;
  closestVerticalDistance: number;
  riskLevel: number;
  timeToClosest: number;
}): string {
  const h = alert.closestHorizontalDistance;
  const v = alert.closestVerticalDistance;
  const tcpa = Number.isFinite(alert.timeToClosest)
    ? `${alert.timeToClosest.toFixed(0)}s`
    : "—";
  return `H ${h.toFixed(1)} NM · V ${Math.round(v)} ft · R${Math.round(alert.riskLevel)} · TCPA ${tcpa}`;
}
