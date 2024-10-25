"use client";
import { useEffect, useRef, useState } from "react";
import { Aircraft } from "../utility/aircraft/aircraftClass";
import loadAtsRoutes from "../utility/AtsRouteManager/atsRoutesLoader";
import { renderMap } from "../utility/AtsRouteManager/routeRenderer";
import { GLOBAL_CONSTANTS } from "../utility/globals/constants";
import { GLOBAL_SETTINGS } from "../utility/globals/settings";
import { fetchAircraftLocation } from "../utility/api/location";
import { controlAircraft } from "../utility/api/controlAircraft";
import { DrawAircraft } from "../utility/aircraft/drawAircraft";
import { SimulationManager } from "../utility/api/simulation";
import { useRouteInfoDisplaySetting } from '@/context/routeInfoDisplaySettingContext';
import { useCenterCoordinate } from "@/context/centerCoordinateContext";
import { useDisplayRange } from "@/context/displayRangeContext";
import { useSelectFixMode } from "@/context/selectFixModeContext";
import { searchFixName } from "@/utility/AtsRouteManager/FixNameSearch";

const RadarCanvas: React.FC = () => {
  const canvasRefs = [useRef<HTMLCanvasElement>(null), useRef<HTMLCanvasElement>(null)];
  const [controllingAircrafts, setControllingAircrafts] = useState<Aircraft[]>([]);
  const controllingAircraftsRef = useRef<Aircraft[]>(controllingAircrafts);
  const [selectedAircraft, setSelectedAircraft] = useState<Aircraft | null>(null);
  const [atsRouteData, setAtsRouteData] = useState<any>(null);
  const [bg, setBg] = useState(0);
  const clickedPositionRef = useRef<{ x: number; y: number } | null>(null);
  const draggingLabelIndexRef = useRef(-1);
  const offsetXRef = useRef(0);
  const offsetYRef = useRef(0);
  const { isDisplaying  } = useRouteInfoDisplaySetting();
  const isDisplayingRef = useRef(isDisplaying);
  const { centerCoordinate } = useCenterCoordinate();
  const centerCoordinateRef = useRef(centerCoordinate);
  const { displayRange } = useDisplayRange();
  const displayRangeRef = useRef(displayRange);
  const { isSelectFixMode } = useSelectFixMode();
  const isSelectFixModeRef = useRef(isSelectFixMode);
  const atsRouteDataRef = useRef(atsRouteData);

  useEffect(() => {
    const canvasContainer = document.getElementsByClassName("radarArea")[0] as HTMLElement;
  
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
    
    const simulationManager = new SimulationManager();
    
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
  }, []);

  useEffect(() => {
    if (atsRouteData) {
      updateCanvas();  // atsRouteDataが設定されてからキャンバス更新を行う
      startUpdatingAircraftLocations();
      startRenderingLoop();
    }
  }, [atsRouteData]);  // atsRouteDataの変更を監視

  const initializeAtsRouteData = async () => {
    try {
      const data = await loadAtsRoutes();
      setAtsRouteData(data);
    } catch (error) {
      console.error("Error loading ATS routes from initializeAtsRouteData:", error);
    }
  };

  useEffect(() => {
    controllingAircraftsRef.current = controllingAircrafts;
  }, [controllingAircrafts]);

  useEffect(() => {
    isDisplayingRef.current = isDisplaying;
  }, [isDisplaying]);

  useEffect(() => {
    centerCoordinateRef.current = centerCoordinate;
  }, [centerCoordinate]);

  useEffect(() => {
    displayRangeRef.current = displayRange;
  }, [displayRange]);

  useEffect(() => {
    isSelectFixModeRef.current = isSelectFixMode;
  }, [isSelectFixMode]);

  useEffect(() => {
    atsRouteDataRef.current = atsRouteData;
  }, [atsRouteData]);

  const updateCanvas = () => {
    if (!atsRouteData) {
      console.error("ATS Route data is missing or incomplete.");
      return;
    }
  
    const ctx = getCanvasContext(bg);
    if (!ctx) return;
  
    clearCanvas(ctx);
    renderMapOnCanvas(ctx);
    renderAircraftsOnCanvas(ctx);
  
    toggleCanvasDisplay();
  };
  
  const getCanvasContext = (bg: number): CanvasRenderingContext2D | null => {
    const canvas = canvasRefs[bg]?.current;
    if (!canvas) {
      console.error(`Canvas element is not found for bg: ${bg}`);
      return null;
    }
  
    return canvas.getContext("2d");
  };
  
  const renderMapOnCanvas = (ctx: CanvasRenderingContext2D) => {
    renderMap(
      atsRouteData.waypoints, atsRouteData.radioNavigationAids,
      atsRouteData.atsLowerRoutes, atsRouteData.rnavRoutes,
      ctx, isDisplayingRef.current, centerCoordinateRef.current,
      displayRangeRef.current
    );
  };
  
  const renderAircraftsOnCanvas = (ctx: CanvasRenderingContext2D) => {
    controllingAircraftsRef.current.forEach((aircraft) => {
      DrawAircraft.drawAircraft(ctx, aircraft);
    });
  };

  const clearCanvas = (ctx: CanvasRenderingContext2D) => {
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, GLOBAL_SETTINGS.canvasWidth, GLOBAL_SETTINGS.canvasHeight);
  };

  const toggleCanvasDisplay = () => {
    canvasRefs[1 - bg].current!.style.display = "none";
    canvasRefs[bg].current!.style.display = "block";
    setBg(1 - bg);
  };

  const startRenderingLoop = () => {
    const renderLoop = () => {
      updateCanvas(); // Update the canvas at each frame
      requestAnimationFrame(renderLoop); // Schedule the next frame
    };
    
    requestAnimationFrame(renderLoop); // Start the first frame
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
      if (!atsRouteData || !atsRouteData.waypoints || !atsRouteData.radioNavigationAids) {
        console.error("atsRouteData is not available");
        return;
      }
      const selectedFixName: string = searchFixName(atsRouteData.waypoints, atsRouteData.radioNavigationAids, {x, y}, centerCoordinateRef.current, displayRangeRef.current);
      if (selectedFixName) {
        console.log("Selected fix:", selectedFixName);
        const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;
        selectedFixNameElement.textContent = selectedFixName;
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

  const isWithinRadius = (x: number, y: number, position: { x: number; y: number }, radius: number) => {
    return (
      x >= position.x - radius &&
      x <= position.x + radius &&
      y >= position.y - radius &&
      y <= position.y + radius
    );
  };

  const isWithinLabelBounds = (x: number, y: number, labelX: number, labelY: number) => {
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

    const aircraft = controllingAircraftsRef.current[draggingLabelIndexRef.current];
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
    const fontElement = document.getElementById("callsign") as HTMLParagraphElement;
    const inputAltitude = document.getElementById("altitude") as HTMLInputElement;
    const inputSpeed = document.getElementById("speed") as HTMLInputElement;
    const inputHeading = document.getElementById("heading") as HTMLInputElement;
    if (fontElement) {
      fontElement.textContent = aircraft.callsign;
    }
    if (inputAltitude) {
      inputAltitude.value = aircraft.instructedVector.altitude.toString();
    }
    if (inputSpeed) {
      inputSpeed.value = aircraft.instructedVector.groundSpeed.toString();
    }
    if (inputHeading) {
      inputHeading.value = aircraft.instructedVector.heading.toString();
    }
  };

  const startUpdatingAircraftLocations = () => {
    const fetchLocationInterval = setInterval(async () => {
      try {
        const currentControllingAircrafts = controllingAircraftsRef.current;
        const updatedAircrafts = await fetchAircraftLocation(currentControllingAircrafts, centerCoordinateRef.current, displayRangeRef.current);
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