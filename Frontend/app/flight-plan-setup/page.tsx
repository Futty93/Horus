"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type { ScenarioJson, ScenarioAircraft } from "@/types/scenario";
import {
  hanedaTemplate,
  exportScenario,
  parseScenarioJson,
  loadScenarioAndStart,
} from "@/utility/api/scenario";
import { suggestRoute } from "@/utility/api/ats";
import loadAtsRoutes from "@/utility/AtsRouteManager/atsRoutesLoader";
import type { JapanOutline } from "@/utility/AtsRouteManager/atsRoutesLoader";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";
import type { Waypoint } from "@/utility/AtsRouteManager/RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "@/utility/AtsRouteManager/RouteInterfaces/RadioNavigationAid";
import type { InitialPositionDto } from "@/types/scenario";
import {
  FlightPlanSetupHeader,
  FlightPlanSetupActionBar,
  FlightPlanSetupNav,
  OdGroupList,
  AircraftTable,
  AircraftRouteEditor,
  AddAircraftForm,
  InitialPositionEditor,
  RoutePreviewMap,
  type AirportOption,
  type NewAircraftDraft,
  type RoutePreviewPickPayload,
} from "@/components/flight-plan-setup";
import {
  deriveRouteByOd,
  groupByOd,
  validateScenarioForStart,
  type ScenarioRouteDef,
} from "@/utility/flightPlanSetup/scenarioPageUtils";

type RouteDef = ScenarioRouteDef;

export default function FlightPlanSetupPage() {
  const router = useRouter();
  const [scenario, setScenario] = useState<ScenarioJson>({ aircraft: [] });
  const [selectedCallsign, setSelectedCallsign] = useState<string | null>(null);
  const selectedAircraft = useMemo(() => {
    const list = scenario.aircraft;
    if (list.length === 0) return null;
    if (selectedCallsign == null) return list[0];
    return (
      list.find((a) => a.flightPlan.callsign === selectedCallsign) ?? list[0]
    );
  }, [scenario.aircraft, selectedCallsign]);
  const [status, setStatus] = useState<string | null>(null);
  const [starting, setStarting] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [loadingSuggest, setLoadingSuggest] = useState(false);
  const [atsRoutes, setAtsRoutes] = useState<{
    waypoints: Waypoint[];
    radioNavigationAids: RadioNavigationAid[];
    atsLowerRoutes: Route[];
    rnavRoutes: Route[];
    japanOutline: JapanOutline;
  }>({
    waypoints: [],
    radioNavigationAids: [],
    atsLowerRoutes: [],
    rnavRoutes: [],
    japanOutline: [],
  });
  const [airportPositions, setAirportPositions] = useState<
    Map<string, { latitude: number; longitude: number }>
  >(new Map());
  const [airports, setAirports] = useState<AirportOption[]>([]);
  const [atsDataError, setAtsDataError] = useState<string | null>(null);

  useEffect(() => {
    loadAtsRoutes()
      .then((data) => {
        setAtsDataError(null);
        setAtsRoutes({
          waypoints: data.waypoints,
          radioNavigationAids: data.radioNavigationAids,
          atsLowerRoutes: data.atsLowerRoutes,
          rnavRoutes: data.rnavRoutes,
          japanOutline: data.japanOutline,
        });
      })
      .catch((e) => {
        setAtsDataError(
          `Failed to load ATS route data. ATS route suggestions and preview may be limited.\n${String(e)}`
        );
      });
  }, []);

  useEffect(() => {
    fetch("/api/ats/airports")
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then(
        (
          arr: {
            icaoCode: string;
            name?: string;
            iataCode?: string;
            latitude: number;
            longitude: number;
          }[]
        ) => {
          const m = new Map<string, { latitude: number; longitude: number }>();
          const options: AirportOption[] = [];
          for (const a of arr ?? []) {
            const icaoCode = a.icaoCode.toUpperCase();
            m.set(icaoCode, {
              latitude: a.latitude,
              longitude: a.longitude,
            });
            options.push({
              icaoCode,
              name: a.name,
              iataCode: a.iataCode,
              latitude: a.latitude,
              longitude: a.longitude,
            });
          }
          options.sort((a, b) => a.icaoCode.localeCompare(b.icaoCode));
          setAirportPositions(m);
          setAirports(options);
        }
      )
      .catch(() => {});
  }, []);

  useEffect(() => {
    const list = scenario.aircraft;
    if (list.length === 0) {
      setSelectedCallsign(null);
      return;
    }
    setSelectedCallsign((prev) => {
      if (prev == null) return list[0].flightPlan.callsign;
      if (list.some((a) => a.flightPlan.callsign === prev)) return prev;
      return list[0].flightPlan.callsign;
    });
  }, [scenario.aircraft]);

  const handleLoadTemplate = useCallback(() => {
    setScenario(hanedaTemplate);
    setStatus("Loaded Haneda template (28 aircraft)");
  }, []);

  const handleLoadTemplateAndSuggest = useCallback(async () => {
    const template = hanedaTemplate;
    const odPairs = groupByOd(template.aircraft);
    setLoadingSuggest(true);
    setStatus("Loading template and suggesting routes...");
    setScenario(template);

    const totalOd = odPairs.length;
    let completed = 0;
    const results = await Promise.all(
      odPairs.map(async (p) => {
        const r = await suggestRoute(p.origin, p.destination);
        completed += 1;
        setStatus(
          `Suggesting routes… ${completed}/${totalOd} (${p.origin}→${p.destination})`
        );
        return r;
      })
    );

    const routeByKey = new Map<string, string[]>();
    const failedPairs: string[] = [];
    odPairs.forEach((p, i) => {
      const key = `${p.origin}→${p.destination}`;
      const r = results[i];
      const ok = "waypoints" in r && r.waypoints.length > 0;
      if (ok) routeByKey.set(key, r.waypoints);
      else failedPairs.push(key);
    });

    const updatedAircraft = template.aircraft.map((a) => {
      const key = `${a.flightPlan.departureAirport}→${a.flightPlan.arrivalAirport}`;
      const waypoints = routeByKey.get(key);
      if (!waypoints?.length) return a;
      return {
        ...a,
        flightPlan: {
          ...a.flightPlan,
          route: waypoints.map((fix) => ({ fix, action: "CONTINUE" as const })),
        },
      };
    });

    setScenario({ ...template, aircraft: updatedAircraft });
    setLoadingSuggest(false);
    const successCount = routeByKey.size;
    let msg =
      successCount === totalOd
        ? `Loaded 28 aircraft with suggested routes (${successCount}/${totalOd} O/D)`
        : `Loaded 28 aircraft, suggested routes for ${successCount}/${totalOd} O/D pairs`;
    if (failedPairs.length > 0) {
      msg += `\nNo suggestion for: ${failedPairs.join(", ")}`;
    }
    setStatus(msg);
  }, []);

  const handleImportJson = useCallback((text: string) => {
    try {
      const sc = parseScenarioJson(text);
      setScenario(sc);
      setStatus(`Imported ${sc.aircraft.length} aircraft`);
    } catch (e) {
      setStatus(`Error: ${String(e)}`);
    }
  }, []);

  const handleExportJson = useCallback(() => {
    exportScenario(scenario);
    setStatus("Exported to JSON");
  }, [scenario]);

  const handleStartWithThis = useCallback(async () => {
    if (scenario.aircraft.length === 0) {
      setStatus("Error: No aircraft to load");
      return;
    }
    const issues = validateScenarioForStart(scenario.aircraft);
    if (issues.length > 0) {
      setStatus(`Error: Cannot start simulation.\n${issues.join("\n")}`);
      return;
    }
    setStarting(true);
    setStatus("Loading scenario...");
    const result = await loadScenarioAndStart(scenario);
    setStarting(false);
    if (result.ok) {
      setStatus("Scenario loaded. Redirecting...");
      router.push("/operator");
    } else {
      setStatus(`Error: ${result.message}`);
    }
  }, [scenario, router]);

  const handleRouteChange = useCallback((key: string, route: RouteDef) => {
    const [origin, destination] = key.split("→");
    const newRoute = route.waypoints.map((fix) => ({
      fix,
      action: "CONTINUE" as const,
    }));
    setScenario((prev) => ({
      ...prev,
      aircraft: prev.aircraft.map((a) => {
        const fp = a.flightPlan;
        if (
          fp.departureAirport !== origin ||
          fp.arrivalAirport !== destination
        ) {
          return a;
        }
        return {
          ...a,
          flightPlan: {
            ...fp,
            route: newRoute,
            cruiseAltitude: route.cruiseAltitude,
            cruiseSpeed: route.cruiseSpeed,
          },
        };
      }),
    }));
  }, []);

  const handleRouteChangeForCallsign = useCallback(
    (callsign: string, route: RouteDef) => {
      const newRoute = route.waypoints.map((fix) => ({
        fix,
        action: "CONTINUE" as const,
      }));
      setScenario((prev) => ({
        ...prev,
        aircraft: prev.aircraft.map((a) => {
          if (a.flightPlan.callsign !== callsign) return a;
          return {
            ...a,
            flightPlan: {
              ...a.flightPlan,
              route: newRoute,
              cruiseAltitude: route.cruiseAltitude,
              cruiseSpeed: route.cruiseSpeed,
            },
          };
        }),
      }));
    },
    []
  );

  const handlePreviewPickRoute = useCallback(
    (payload: RoutePreviewPickPayload) => {
      if (!selectedAircraft) return;
      const fp = selectedAircraft.flightPlan;
      const chain = new Set([
        fp.departureAirport.toUpperCase(),
        ...fp.route.map((w) => w.fix.toUpperCase()),
        fp.arrivalAirport.toUpperCase(),
      ]);
      const up = payload.fixName.trim().toUpperCase();
      if (chain.has(up)) {
        setStatus(`${payload.fixName} is already on this route`);
        return;
      }
      const cruise = fp.route.map((w) => w.fix);
      const waypointsForSave =
        payload.kind === "append"
          ? [...cruise, payload.fixName]
          : [
              ...cruise.slice(0, payload.insertIndex),
              payload.fixName,
              ...cruise.slice(payload.insertIndex),
            ];
      handleRouteChangeForCallsign(fp.callsign, {
        waypoints: waypointsForSave,
        cruiseAltitude: fp.cruiseAltitude,
        cruiseSpeed: fp.cruiseSpeed,
      });
      if (payload.kind === "append") {
        setStatus(`Appended ${payload.fixName} from preview map`);
      } else {
        setStatus(
          `Inserted ${payload.fixName} into cruise route at position ${payload.insertIndex + 1}`
        );
      }
    },
    [selectedAircraft, handleRouteChangeForCallsign]
  );

  const handleSelectAircraft = useCallback((a: ScenarioAircraft) => {
    setSelectedCallsign(a.flightPlan.callsign);
  }, []);

  const handleDeleteAircraft = useCallback((callsign: string) => {
    if (!confirm(`Delete aircraft ${callsign}?`)) return;
    setScenario((prev) => ({
      ...prev,
      aircraft: prev.aircraft.filter((a) => a.flightPlan.callsign !== callsign),
    }));
    setSelectedCallsign((prev) => (prev === callsign ? null : prev));
    setStatus(`Deleted ${callsign}`);
  }, []);

  const handleAddAircraft = useCallback(
    async (draft: NewAircraftDraft) => {
      const pos = airportPositions.get(draft.departureAirport);
      const aircraft: ScenarioAircraft = {
        flightPlan: {
          callsign: draft.callsign,
          departureAirport: draft.departureAirport,
          arrivalAirport: draft.arrivalAirport,
          cruiseAltitude: 31000,
          cruiseSpeed: 450,
          route: [],
        },
        initialPosition: {
          latitude: pos?.latitude ?? 35.5,
          longitude: pos?.longitude ?? 139.8,
          altitude: 31000,
          heading: 90,
          groundSpeed: 450,
          verticalSpeed: 0,
        },
      };
      setScenario((prev) => ({
        ...prev,
        aircraft: [...prev.aircraft, aircraft],
      }));
      setSelectedCallsign(draft.callsign);
      setShowAddForm(false);
      setStatus(
        `Placed ${draft.callsign} at ${draft.departureAirport}. Looking up a route…`
      );
      const result = await suggestRoute(
        draft.departureAirport,
        draft.arrivalAirport
      );
      if ("waypoints" in result && result.waypoints.length > 0) {
        const fixes = result.waypoints;
        setScenario((prev) => ({
          ...prev,
          aircraft: prev.aircraft.map((item) => {
            if (item.flightPlan.callsign !== draft.callsign) return item;
            if (item.flightPlan.route.length > 0) return item;
            return {
              ...item,
              flightPlan: {
                ...item.flightPlan,
                route: fixes.map((fix) => ({
                  fix,
                  action: "CONTINUE" as const,
                })),
              },
            };
          }),
        }));
        setStatus(
          `Placed ${draft.callsign} at ${draft.departureAirport} with ${fixes.length} fixes. Drag Now on the chart to move the start.`
        );
        return;
      }
      const detail = "error" in result ? result.error : "no route found";
      setStatus(
        `Placed ${draft.callsign} at ${draft.departureAirport}. Route suggestion failed: ${detail}`
      );
    },
    [airportPositions]
  );

  const handleUpdateAircraft = useCallback(
    (callsign: string, pos: Partial<InitialPositionDto>) => {
      setScenario((prev) => {
        const updated = prev.aircraft.map((a) => {
          if (a.flightPlan.callsign !== callsign) return a;
          return {
            ...a,
            initialPosition: { ...a.initialPosition, ...pos },
          };
        });
        return { ...prev, aircraft: updated };
      });
    },
    []
  );

  const odPairs = useMemo(
    () => groupByOd(scenario.aircraft),
    [scenario.aircraft]
  );
  const routeByOd = useMemo(
    () => deriveRouteByOd(scenario.aircraft),
    [scenario.aircraft]
  );

  const editor = (
    <AircraftRouteEditor
      aircraft={selectedAircraft}
      atsRoutes={{
        atsLowerRoutes: atsRoutes.atsLowerRoutes,
        rnavRoutes: atsRoutes.rnavRoutes,
      }}
      fixSources={{
        waypoints: atsRoutes.waypoints,
        radioNavAids: atsRoutes.radioNavigationAids,
      }}
      onApplyRoute={(route, applyOptions) => {
        const callsign =
          applyOptions?.callsign ?? selectedAircraft?.flightPlan.callsign;
        if (callsign) {
          handleRouteChangeForCallsign(callsign, route);
        }
      }}
      onSuggestStatus={setStatus}
    />
  );

  return (
    <div className="flex h-screen overflow-hidden bg-[#141210] text-[#f4efe4] font-[family-name:var(--font-geist-sans)]">
      <div className="flex w-[440px] shrink-0 flex-col bg-[#f4efe4] text-[#1c1915]">
        <div className="shrink-0 border-b border-[#e4d8c4] px-5 py-4">
          <FlightPlanSetupHeader count={scenario.aircraft.length} />
          <FlightPlanSetupActionBar
            onLoadTemplate={handleLoadTemplate}
            onLoadTemplateAndSuggest={handleLoadTemplateAndSuggest}
            onImportJson={handleImportJson}
            onExportJson={handleExportJson}
            loadingSuggest={loadingSuggest}
            hasAircraft={scenario.aircraft.length > 0}
            dataLoadWarning={atsDataError}
          />
        </div>
        <div className="min-h-0 flex-1 space-y-8 overflow-y-auto px-5 py-5">
          {scenario.aircraft.length === 0 || showAddForm ? (
            <AddAircraftForm
              existingCallsigns={scenario.aircraft.map(
                (aircraft) => aircraft.flightPlan.callsign
              )}
              airports={airports}
              onSubmit={handleAddAircraft}
              onCancel={
                scenario.aircraft.length === 0
                  ? undefined
                  : () => setShowAddForm(false)
              }
              onStatus={setStatus}
            />
          ) : (
            <button
              type="button"
              onClick={() => setShowAddForm(true)}
              className="text-sm font-semibold text-[#c2410c] hover:underline"
            >
              Add another aircraft
            </button>
          )}
          {scenario.aircraft.length > 0 && (
            <>
              <section className="space-y-3">
                <h2 className="text-xs font-semibold uppercase tracking-[0.16em] text-[#8a7d6b]">
                  Aircraft
                </h2>
                <AircraftTable
                  aircraft={scenario.aircraft}
                  selectedCallsign={
                    selectedAircraft?.flightPlan.callsign ?? null
                  }
                  onSelectAircraft={handleSelectAircraft}
                  onDeleteAircraft={handleDeleteAircraft}
                />
              </section>
              {editor}
              <InitialPositionEditor
                aircraft={selectedAircraft}
                onUpdate={handleUpdateAircraft}
              />
              <OdGroupList
                odPairs={odPairs}
                selectedCallsign={selectedAircraft?.flightPlan.callsign ?? null}
                onSelectAircraft={handleSelectAircraft}
                onSuggestStatus={setStatus}
                atsRoutes={{
                  atsLowerRoutes: atsRoutes.atsLowerRoutes,
                  rnavRoutes: atsRoutes.rnavRoutes,
                }}
                routeByOd={routeByOd}
                onRouteChange={handleRouteChange}
              />
            </>
          )}
        </div>
        <div className="shrink-0 space-y-3 border-t border-[#e4d8c4] px-5 py-4">
          {status && (
            <p
              className={`text-sm whitespace-pre-line ${
                status.startsWith("Error") || status.includes("failed")
                  ? "text-[#b42318]"
                  : "text-[#3f6212]"
              }`}
            >
              {status}
            </p>
          )}
          <button
            type="button"
            onClick={handleStartWithThis}
            disabled={scenario.aircraft.length === 0 || starting}
            className="w-full rounded-full bg-[#c2410c] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#9a3412] disabled:opacity-40"
          >
            {starting ? "Starting…" : "Start simulation"}
          </button>
          <FlightPlanSetupNav />
        </div>
      </div>
      <main className="min-w-0 flex-1 p-4">
        <RoutePreviewMap
          className="h-full overflow-hidden rounded-[28px] ring-1 ring-white/10"
          selectedAircraft={selectedAircraft}
          waypoints={atsRoutes.waypoints}
          radioNavAids={atsRoutes.radioNavigationAids}
          airportPositions={airportPositions}
          japanOutline={atsRoutes.japanOutline}
          rnavRoutes={atsRoutes.rnavRoutes}
          atsLowerRoutes={atsRoutes.atsLowerRoutes}
          onPickRoute={handlePreviewPickRoute}
          onPickHint={setStatus}
          onInitialPositionGeoChange={
            selectedAircraft
              ? (latitude, longitude) =>
                  handleUpdateAircraft(selectedAircraft.flightPlan.callsign, {
                    latitude,
                    longitude,
                  })
              : undefined
          }
        />
      </main>
    </div>
  );
}
