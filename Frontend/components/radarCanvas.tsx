"use client";
import React, { useEffect, useMemo, useRef, useState } from "react";
import { Aircraft } from "@/utility/aircraft/aircraftClass";
import loadAtsRoutes from "@/utility/AtsRouteManager/atsRoutesLoader";
import {
  renderMap,
  drawRangeRings,
} from "@/utility/AtsRouteManager/routeRenderer";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";
import { GLOBAL_SETTINGS } from "@/utility/globals/settings";
import { fetchAircraftLocation } from "@/utility/api/location";
import { DrawAircraft } from "@/utility/aircraft/drawAircraft";
import { useRouteInfoDisplaySetting } from "@/context/routeInfoDisplaySettingContext";
import { useCenterCoordinate } from "@/context/centerCoordinateContext";
import { useDisplayRange } from "@/context/displayRangeContext";
import { useVelocityVectorLookahead } from "@/context/velocityVectorLookaheadContext";
import { useRangeRingsSetting } from "@/context/rangeRingsSettingContext";
import {
  useDataBlockDisplaySetting,
  type DataBlockDisplaySetting,
} from "@/context/dataBlockDisplaySettingContext";
import { useSelectFixMode } from "@/context/selectFixModeContext";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { searchFixName } from "@/utility/AtsRouteManager/FixNameSearch";
import { usePathname } from "next/navigation";

const RadarCanvas: React.FC = () => {
  const canvasRefs = [
    useRef<HTMLCanvasElement>(null),
    useRef<HTMLCanvasElement>(null),
  ];
  const [controllingAircrafts, setControllingAircrafts] = useState<Aircraft[]>(
    []
  );
  const controllingAircraftsRef = useRef<Aircraft[]>(controllingAircrafts);
  const [selectedAircraft, setSelectedAircraft] = useState<Aircraft | null>(
    null
  );
  const selectedAircraftRef = useRef(selectedAircraft);
  const [atsRouteData, setAtsRouteData] = useState<Awaited<
    ReturnType<typeof loadAtsRoutes>
  > | null>(null);
  const [bg, setBg] = useState(0);
  const bgRef = useRef(0);
  const draggingLabelIndexRef = useRef(-1);
  const offsetXRef = useRef(0);
  const offsetYRef = useRef(0);
  const { isDisplaying } = useRouteInfoDisplaySetting();
  const isDisplayingRef = useRef(isDisplaying);
  const { centerCoordinate } = useCenterCoordinate();
  const centerCoordinateRef = useRef(centerCoordinate);
  const { displayRange } = useDisplayRange();
  const displayRangeRef = useRef(displayRange);
  const { durationMinutes } = useVelocityVectorLookahead();
  const velocityVectorDurationRef = useRef(durationMinutes);
  const { rangeRingsSetting } = useRangeRingsSetting();
  const rangeRingsSettingRef = useRef(rangeRingsSetting);
  const { dataBlockDisplaySetting } = useDataBlockDisplaySetting();
  const pathname = usePathname();
  const dataBlockRadarSetting = useMemo<DataBlockDisplaySetting>(() => {
    const operator = pathname === "/operator";
    return {
      ...dataBlockDisplaySetting,
      atcClearanceMemo: operator
        ? false
        : dataBlockDisplaySetting.atcClearanceMemo,
    };
  }, [pathname, dataBlockDisplaySetting]);
  const dataBlockDisplaySettingRef = useRef(dataBlockRadarSetting);
  const { isSelectFixMode, setSelectedFixName } = useSelectFixMode();
  const {
    setCallsign,
    setInstructedVector,
    registerApplyInstructedVectorHandler,
  } = useSelectedAircraft();
  const isSelectFixModeRef = useRef(isSelectFixMode);
  const atsRouteDataRef = useRef(atsRouteData);
  const controllerClearanceAltitudeRowRef = useRef(false);
  const pathnameRef = useRef(pathname);

  // Refs for rAF / setInterval / event handlers: sync during render instead of mirroring in useEffect.
  isDisplayingRef.current = isDisplaying;
  centerCoordinateRef.current = centerCoordinate;
  displayRangeRef.current = displayRange;
  velocityVectorDurationRef.current = durationMinutes;
  rangeRingsSettingRef.current = rangeRingsSetting;
  dataBlockDisplaySettingRef.current = dataBlockRadarSetting;
  isSelectFixModeRef.current = isSelectFixMode;
  atsRouteDataRef.current = atsRouteData;
  selectedAircraftRef.current = selectedAircraft;
  controllingAircraftsRef.current = controllingAircrafts;
  controllerClearanceAltitudeRowRef.current = pathname === "/controller";
  pathnameRef.current = pathname;
  bgRef.current = bg;

  useEffect(() => {
    const canvasContainer = document.getElementsByClassName(
      "radarArea"
    )[0] as HTMLElement;

    // Initialize canvas dimensions
    canvasRefs.forEach((canvasRef) => {
      const canvas = canvasRef.current;
      if (canvas) {
        canvas.width = canvasContainer.clientWidth;
        canvas.height = canvasContainer.clientHeight;
      }
    });

    GLOBAL_SETTINGS.canvasWidth = canvasRefs[0].current!.width;
    GLOBAL_SETTINGS.canvasHeight = canvasRefs[0].current!.height;

    // Load ATS route data and initialize event listeners
    initializeAtsRouteData();

    const handleMouseEvents = (canvas: HTMLCanvasElement) => {
      canvas.addEventListener("mousedown", onMouseDown);
      canvas.addEventListener("mousemove", onMouseMove);
      canvas.addEventListener("mouseup", onMouseUp);
    };

    const removeMouseEvents = (canvas: HTMLCanvasElement) => {
      canvas.removeEventListener("mousedown", onMouseDown);
      canvas.removeEventListener("mousemove", onMouseMove);
      canvas.removeEventListener("mouseup", onMouseUp);
    };

    canvasRefs.forEach((canvasRef) => {
      const canvas = canvasRef.current;
      if (canvas) handleMouseEvents(canvas);
    });

    return () => {
      canvasRefs.forEach((canvasRef) => {
        const canvas = canvasRef.current;
        if (canvas) removeMouseEvents(canvas);
      });
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- setup on mount only; canvasRefs/onMouseDown are refs/stable
  }, []);

  useEffect(() => {
    if (!atsRouteData) return;

    updateCanvas();
    const stopUpdating = startUpdatingAircraftLocations();

    let rafId = 0;
    let running = true;
    const renderLoop = () => {
      if (!running) return;
      updateCanvas();
      rafId = requestAnimationFrame(renderLoop);
    };
    rafId = requestAnimationFrame(renderLoop);

    return () => {
      running = false;
      cancelAnimationFrame(rafId);
      stopUpdating();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- intentional: run when atsRouteData is set; functions are stable
  }, [atsRouteData]);

  const initializeAtsRouteData = async () => {
    try {
      const data = await loadAtsRoutes();
      setAtsRouteData(data);
    } catch (error) {
      console.error(
        "Error loading ATS routes from initializeAtsRouteData:",
        error
      );
    }
  };

  useEffect(() => {
    const unregister = registerApplyInstructedVectorHandler(
      (callsign, instructedVector) => {
        const aircraft = controllingAircraftsRef.current.find(
          (a) => a.callsign === callsign
        );
        if (aircraft) {
          aircraft.instructedVector = {
            heading: instructedVector.heading,
            groundSpeed: instructedVector.groundSpeed,
            altitude: instructedVector.altitude,
          };
          setControllingAircrafts([...controllingAircraftsRef.current]);
        }
      }
    );
    return unregister;
  }, [registerApplyInstructedVectorHandler]);

  const updateCanvas = () => {
    if (!atsRouteDataRef.current) {
      console.error("ATS Route data is missing or incomplete.");
      return;
    }

    const idx = bgRef.current;
    const ctx = getCanvasContext(idx);
    if (!ctx) return;

    const canvas = canvasRefs[idx]?.current;
    if (!canvas) return;

    clearCanvas(ctx, canvas);
    renderMapOnCanvas(ctx);
    renderAircraftsOnCanvas(ctx);

    toggleCanvasDisplay();
  };

  const getCanvasContext = (index: number): CanvasRenderingContext2D | null => {
    const canvas = canvasRefs[index]?.current;
    if (!canvas) {
      console.error(`Canvas element is not found for index: ${index}`);
      return null;
    }

    return canvas.getContext("2d");
  };

  const renderMapOnCanvas = (ctx: CanvasRenderingContext2D) => {
    if (!atsRouteData) return;
    renderMap(
      atsRouteData.waypoints,
      atsRouteData.radioNavigationAids,
      atsRouteData.atsLowerRoutes,
      atsRouteData.rnavRoutes,
      ctx,
      isDisplayingRef.current,
      centerCoordinateRef.current,
      displayRangeRef.current
    );
    drawRangeRings(ctx, displayRangeRef.current, rangeRingsSettingRef.current);
  };

  const renderAircraftsOnCanvas = (ctx: CanvasRenderingContext2D) => {
    controllingAircraftsRef.current.forEach((aircraft) => {
      DrawAircraft.drawAircraft(
        ctx,
        aircraft,
        displayRangeRef.current,
        centerCoordinateRef.current,
        dataBlockDisplaySettingRef.current,
        controllerClearanceAltitudeRowRef.current,
        velocityVectorDurationRef.current
      );
    });
  };

  const clearCanvas = (
    ctx: CanvasRenderingContext2D,
    canvas: HTMLCanvasElement
  ) => {
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
  };

  const toggleCanvasDisplay = () => {
    const idx = bgRef.current;
    const back = canvasRefs[1 - idx].current;
    const front = canvasRefs[idx].current;
    if (back) back.style.display = "none";
    if (front) front.style.display = "block";
    const next = 1 - idx;
    bgRef.current = next;
    setBg(next);
  };

  const onMouseDown = (event: MouseEvent) => {
    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const aircraftRadius = 30;
    console.log("Mouse down at", x, y);

    const currentControllingAircrafts = controllingAircraftsRef.current;

    if (isSelectFixModeRef.current.selectFixMode) {
      // フィックス選択モードの場合
      const atsRouteData = atsRouteDataRef.current;
      if (
        !atsRouteData ||
        !atsRouteData.waypoints ||
        !atsRouteData.radioNavigationAids
      ) {
        console.error("atsRouteData is not available");
        return;
      }
      const fixName = searchFixName(
        atsRouteData.waypoints,
        atsRouteData.radioNavigationAids,
        { x, y },
        centerCoordinateRef.current,
        displayRangeRef.current
      );
      if (fixName) {
        setSelectedFixName(fixName);
      }
      return;
    }

    for (let index = 0; index < currentControllingAircrafts.length; index++) {
      const aircraft = currentControllingAircrafts[index];
      const { position, label, callsign } = aircraft;
      const labelX = position.x + label.x;
      const labelY = position.y - label.y;

      if (isWithinRadius(x, y, position, aircraftRadius)) {
        changeDisplayAircraftInfo(aircraft);
        setSelectedAircraft(aircraft);
        console.log("Clicked on aircraft", aircraft);
        break; // 条件が満たされた場合はループを抜ける
      }

      if (isWithinLabelBounds(x, y, labelX, labelY)) {
        console.log("Clicked on label", callsign);
        draggingLabelIndexRef.current = index;
        offsetXRef.current = x - labelX;
        offsetYRef.current = y - labelY;
        break; // 条件が満たされた場合はループを抜ける
      }
    }
  };

  const isWithinRadius = (
    x: number,
    y: number,
    position: { x: number; y: number },
    radius: number
  ) => {
    return (
      x >= position.x - radius &&
      x <= position.x + radius &&
      y >= position.y - radius &&
      y <= position.y + radius
    );
  };

  const isWithinLabelBounds = (
    x: number,
    y: number,
    labelX: number,
    labelY: number
  ) => {
    return (
      x >= labelX - 5 &&
      x <= labelX + 70 &&
      y >= labelY - 20 &&
      y <= labelY + 40
    );
  };

  const onMouseMove = (event: MouseEvent) => {
    if (draggingLabelIndexRef.current === -1) return;

    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    const aircraft =
      controllingAircraftsRef.current[draggingLabelIndexRef.current];
    const label = aircraft.label;
    const aircraftPosition = aircraft.position;

    label.x = mouseX - offsetXRef.current - aircraftPosition.x;
    label.y = aircraftPosition.y - (mouseY - offsetYRef.current);

    // Update the label position
    setControllingAircrafts([...controllingAircraftsRef.current]);
  };

  const onMouseUp = () => {
    draggingLabelIndexRef.current = -1;
    console.log("Mouse up");
  };

  const changeDisplayAircraftInfo = (aircraft: Aircraft) => {
    setCallsign(aircraft.callsign);
    setInstructedVector({
      altitude: Math.round(aircraft.instructedVector.altitude),
      groundSpeed: Math.round(aircraft.instructedVector.groundSpeed),
      heading: Math.round(aircraft.instructedVector.heading),
    });
  };

  const startUpdatingAircraftLocations = () => {
    const fetchLocationInterval = setInterval(async () => {
      try {
        const currentControllingAircrafts = controllingAircraftsRef.current;
        const updatedAircrafts = await fetchAircraftLocation(
          currentControllingAircrafts,
          centerCoordinateRef.current,
          displayRangeRef.current,
          pathnameRef.current
        );
        // console.log("updatedAircrafts", updatedAircrafts);

        // 最新の controllingAircrafts を取得して状態を更新
        setControllingAircrafts(() => {
          return updatedAircrafts;
        });
      } catch (error) {
        console.error("Error fetching or setting aircraft locations:", error);
      }
    }, GLOBAL_CONSTANTS.LOCATION_UPDATE_INTERVAL);

    return () => clearInterval(fetchLocationInterval);
  };

  return (
    <div className="radarArea relative w-full">
      <canvas ref={canvasRefs[0]} className="w-full h-full bg-black"></canvas>
      <canvas ref={canvasRefs[1]} className="w-full hidden"></canvas>
    </div>
  );
};

export default RadarCanvas;
