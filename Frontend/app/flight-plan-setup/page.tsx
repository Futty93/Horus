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
        (arr: { icaoCode: string; latitude: number; longitude: number }[]) => {
          const m = new Map<string, { latitude: number; longitude: number }>();
          for (const a of arr ?? []) {
            m.set(a.icaoCode.toUpperCase(), {
              latitude: a.latitude,
              longitude: a.longitude,
            });
          }
          setAirportPositions(m);
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

  const handleAddAircraft = useCallback((aircraft: ScenarioAircraft) => {
    setScenario((prev) => ({
      ...prev,
      aircraft: [...prev.aircraft, aircraft],
    }));
    setSelectedCallsign(aircraft.flightPlan.callsign);
    setShowAddForm(false);
  }, []);

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

  const renderContent = () => {
    if (scenario.aircraft.length === 0) {
      return (
        <div className="space-y-4">
          <p className="text-atc-text-muted">
            Load Haneda Template or Import JSON to get started.
            {!showAddForm && (
              <>
                {" "}
                Or{" "}
                <button
                  type="button"
                  onClick={() => setShowAddForm(true)}
                  className="font-bold text-atc-accent hover:underline"
                >
                  Add aircraft
                </button>
                .
              </>
            )}
          </p>
          {showAddForm && (
            <AddAircraftForm
              existingCallsigns={[]}
              airportPositions={airportPositions}
              onSubmit={handleAddAircraft}
              onCancel={() => setShowAddForm(false)}
              onStatus={setStatus}
            />
          )}
        </div>
      );
    }
    return (
      <>
        <section className="space-y-2">
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
            onApplyRoute={(route) => {
              if (selectedAircraft) {
                handleRouteChangeForCallsign(
                  selectedAircraft.flightPlan.callsign,
                  route
                );
              }
            }}
            onSuggestStatus={setStatus}
          />
        </section>
        <section>
          <div className="flex items-center justify-between mb-1">
            <h2 className="font-mono text-sm font-bold">Aircraft</h2>
            {!showAddForm ? (
              <button
                type="button"
                onClick={() => setShowAddForm(true)}
                className="px-3 py-1.5 text-xs font-bold bg-atc-surface border border-atc-border rounded
                           text-atc-text hover:border-atc-accent"
              >
                Add aircraft
              </button>
            ) : null}
          </div>
          <p className="text-xs text-atc-text-muted mb-3">
            Click a row to select; route editor and the large map follow that
            aircraft.
          </p>
          {showAddForm && (
            <div className="mb-4">
              <AddAircraftForm
                existingCallsigns={scenario.aircraft.map(
                  (a) => a.flightPlan.callsign
                )}
                airportPositions={airportPositions}
                onSubmit={handleAddAircraft}
                onCancel={() => setShowAddForm(false)}
                onStatus={setStatus}
              />
            </div>
          )}
          <AircraftTable
            aircraft={scenario.aircraft}
            selectedCallsign={selectedAircraft?.flightPlan.callsign ?? null}
            onSelectAircraft={handleSelectAircraft}
            onDeleteAircraft={handleDeleteAircraft}
          />
        </section>
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
    );
  };

  return (
    <div className="min-h-screen bg-atc-bg text-atc-text">
      <div className="flex h-screen overflow-hidden">
        <div
          className="w-[400px] shrink-0 overflow-y-auto border-r border-atc-border p-4
                     bg-atc-bg space-y-6"
        >
          <FlightPlanSetupHeader />
          <FlightPlanSetupActionBar
            onLoadTemplate={handleLoadTemplate}
            onLoadTemplateAndSuggest={handleLoadTemplateAndSuggest}
            onImportJson={handleImportJson}
            onExportJson={handleExportJson}
            onStartWithThis={handleStartWithThis}
            status={status}
            starting={starting}
            loadingSuggest={loadingSuggest}
            hasAircraft={scenario.aircraft.length > 0}
            dataLoadWarning={atsDataError}
          />
          {renderContent()}
          <FlightPlanSetupNav />
        </div>
        <aside
          className="flex-1 min-w-0 min-h-0 flex flex-col gap-3 p-4 overflow-hidden
                     bg-atc-surface/30"
        >
          <div className="flex-1 min-h-[50vh] min-w-0 flex flex-col">
            <RoutePreviewMap
              className="h-full min-h-0 flex-1"
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
                      handleUpdateAircraft(
                        selectedAircraft.flightPlan.callsign,
                        { latitude, longitude }
                      )
                  : undefined
              }
            />
          </div>
          <div className="shrink-0 max-h-[40vh] overflow-y-auto border border-atc-border rounded-lg bg-atc-bg/80 p-3">
            <InitialPositionEditor
              aircraft={selectedAircraft}
              onUpdate={handleUpdateAircraft}
            />
          </div>
        </aside>
      </div>
    </div>
  );
}
