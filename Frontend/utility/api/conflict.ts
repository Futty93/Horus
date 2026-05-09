/** Distances match backend `RiskAssessment`: horizontal NM, vertical ft, TCPA seconds. */
export interface ConflictAlertDto {
  callsignA: string;
  callsignB: string;
  riskLevel: number;
  alertLevel: string;
  /** Seconds until closest point of approach (may be non-finite when no CPA). */
  timeToClosest: number;
  /** Nautical miles. */
  closestHorizontalDistance: number;
  /** Feet. */
  closestVerticalDistance: number;
  conflictPredicted: boolean;
}

export interface ConflictStatisticsDto {
  totalConflicts: number;
  safeCount: number;
  whiteConflictCount: number;
  redConflictCount: number;
  separationViolationCount: number;
  maxRiskLevel: number;
  avgRiskLevel: number;
}

async function fetchJson<T>(path: string): Promise<T | null> {
  try {
    const response = await fetch(path, {
      cache: "no-store",
      headers: { Accept: "application/json" },
    });
    if (!response.ok) {
      return null;
    }
    return (await response.json()) as T;
  } catch {
    return null;
  }
}

export async function fetchConflictStatistics(): Promise<ConflictStatisticsDto | null> {
  return fetchJson<ConflictStatisticsDto>("/api/conflict/statistics");
}

export async function fetchConflictAll(): Promise<ConflictAlertDto[] | null> {
  return fetchJson<ConflictAlertDto[]>("/api/conflict/all");
}

export async function fetchConflictCritical(): Promise<
  ConflictAlertDto[] | null
> {
  return fetchJson<ConflictAlertDto[]>("/api/conflict/critical");
}

export async function fetchConflictViolations(): Promise<
  ConflictAlertDto[] | null
> {
  return fetchJson<ConflictAlertDto[]>("/api/conflict/violations");
}

export async function fetchAircraftConflicts(
  callsign: string
): Promise<ConflictAlertDto[] | null> {
  const encoded = encodeURIComponent(callsign);
  return fetchJson<ConflictAlertDto[]>(`/api/conflict/aircraft/${encoded}`);
}
