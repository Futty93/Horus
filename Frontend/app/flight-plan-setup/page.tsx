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
  AddAircraftForm,
  InitialPositionEditor,
  RoutePreviewMap,
} from "@/components/flight-plan-setup";

type RouteDef = {
  waypoints: string[];
  cruiseAltitude: number;
  cruiseSpeed: number;
};

function groupByOd(aircraft: ScenarioAircraft[]): {
  origin: string;
  destination: string;
  aircraft: ScenarioAircraft[];
}[] {
  const byKey = new Map<string, ScenarioAircraft[]>();
  for (const a of aircraft) {
    const key = `${a.flightPlan.departureAirport}→${a.flightPlan.arrivalAirport}`;
    const list = byKey.get(key) ?? [];
    list.push(a);
    byKey.set(key, list);
  }
  return Array.from(byKey.entries()).map(([key, ac]) => {
    const [origin, destination] = key.split("→");
    return { origin, destination, aircraft: ac };
  });
}

function deriveRouteByOd(aircraft: ScenarioAircraft[]): Map<string, RouteDef> {
  const m = new Map<string, RouteDef>();
  for (const a of aircraft) {
    const key = `${a.flightPlan.departureAirport}→${a.flightPlan.arrivalAirport}`;
    if (m.has(key)) continue;
    m.set(key, {
      waypoints: a.flightPlan.route.map((wp) => wp.fix),
      cruiseAltitude: a.flightPlan.cruiseAltitude,
      cruiseSpeed: a.flightPlan.cruiseSpeed,
    });
  }
  return m;
}

export default function FlightPlanSetupPage() {
  const router = useRouter();
  const [scenario, setScenario] = useState<ScenarioJson>({ aircraft: [] });
  const [selectedAircraft, setSelectedAircraft] =
    useState<ScenarioAircraft | null>(null);
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

  useEffect(() => {
    loadAtsRoutes()
      .then((data) =>
        setAtsRoutes({
          waypoints: data.waypoints,
          radioNavigationAids: data.radioNavigationAids,
          atsLowerRoutes: data.atsLowerRoutes,
          rnavRoutes: data.rnavRoutes,
          japanOutline: data.japanOutline,
        })
      )
      .catch(() => {});
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

  const handleLoadTemplate = useCallback(() => {
    setScenario(hanedaTemplate);
    setSelectedAircraft(null);
    setStatus("Loaded Haneda template (28 aircraft)");
  }, []);

  const handleLoadTemplateAndSuggest = useCallback(async () => {
    const template = hanedaTemplate;
    const odPairs = groupByOd(template.aircraft);
    setLoadingSuggest(true);
    setStatus("Loading template and suggesting routes...");
    setScenario(template);
    setSelectedAircraft(null);

    const results = await Promise.all(
      odPairs.map((p) => suggestRoute(p.origin, p.destination))
    );

    const routeByKey = new Map<string, string[]>();
    odPairs.forEach((p, i) => {
      const key = `${p.origin}→${p.destination}`;
      const r = results[i];
      const ok = "waypoints" in r && r.waypoints.length > 0;
      if (ok) routeByKey.set(key, r.waypoints);
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
    const totalOd = odPairs.length;
    setStatus(
      successCount === totalOd
        ? `Loaded 28 aircraft with suggested routes (${successCount}/${totalOd} O/D)`
        : `Loaded 28 aircraft, suggested routes for ${successCount}/${totalOd} O/D pairs`
    );
  }, []);

  const handleImportJson = useCallback((text: string) => {
    try {
      const sc = parseScenarioJson(text);
      setScenario(sc);
      setSelectedAircraft(null);
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

  const handleSelectAircraft = useCallback((a: ScenarioAircraft) => {
    setSelectedAircraft(a);
  }, []);

  const handleDeleteAircraft = useCallback((callsign: string) => {
    if (!confirm(`Delete aircraft ${callsign}?`)) return;
    setScenario((prev) => ({
      ...prev,
      aircraft: prev.aircraft.filter((a) => a.flightPlan.callsign !== callsign),
    }));
    setSelectedAircraft((prev) =>
      prev?.flightPlan.callsign === callsign ? null : prev
    );
    setStatus(`Deleted ${callsign}`);
  }, []);

  const handleAddAircraft = useCallback((aircraft: ScenarioAircraft) => {
    setScenario((prev) => ({
      ...prev,
      aircraft: [...prev.aircraft, aircraft],
    }));
    setSelectedAircraft(aircraft);
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
      setSelectedAircraft((prev) => {
        if (!prev || prev.flightPlan.callsign !== callsign) return prev;
        return {
          ...prev,
          initialPosition: { ...prev.initialPosition, ...pos },
        };
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
        <section>
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-mono text-sm font-bold">Aircraft Table</h2>
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
      </>
    );
  };

  return (
    <div className="min-h-screen bg-atc-bg text-atc-text">
      <div className="flex h-screen overflow-hidden">
        <div className="flex-1 overflow-y-auto p-6">
          <div className="max-w-4xl mx-auto space-y-6">
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
            />
            {renderContent()}
            <FlightPlanSetupNav />
          </div>
        </div>
        <aside className="w-[480px] flex-shrink-0 border-l border-atc-border p-4 overflow-y-auto bg-atc-surface/30">
          <div className="sticky top-4 space-y-4">
            <RoutePreviewMap
              selectedAircraft={selectedAircraft}
              waypoints={atsRoutes.waypoints}
              radioNavAids={atsRoutes.radioNavigationAids}
              airportPositions={airportPositions}
              japanOutline={atsRoutes.japanOutline}
            />
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
