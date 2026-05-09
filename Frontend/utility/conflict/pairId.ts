export function getOtherCallsignFromPair(
  pair: { callsignA: string; callsignB: string },
  ownCallsign: string
): string {
  return pair.callsignA === ownCallsign ? pair.callsignB : pair.callsignA;
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
