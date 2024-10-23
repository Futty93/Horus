"use client";
import { useEffect, useRef, useState } from "react";
import { Aircraft } from "../utility/aircraft/aircraftClass";
// import { Waypoint } from "../utility/AtsRouteManager/RouteInterfaces/Waypoint";
import { CoordinateManager } from "../utility/coordinateManager/CoordinateManager";
import loadAtsRoutes from "../utility/AtsRouteManager/atsRoutesLoader";
import { renderMap } from "../utility/AtsRouteManager/routeRenderer";
import { GLOBAL_CONSTANTS } from "../utility/globals/constants";
import { GLOBAL_SETTINGS } from "../utility/globals/settings";
import { fetchAircraftLocation } from "../utility/api/location";
import { controlAircraft } from "../utility/api/controlAircraft";
import { DrawAircraft } from "../utility/aircraft/drawAircraft";
import { SimulationManager } from "../utility/api/simulation";
import { useRouteInfoDisplaySetting } from '@/context/routeInfoDisplaySettingContext';

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

    // Load ATS route data and set up event listeners
    initializeAtsRouteData();

    const simulationManager = new SimulationManager();

    // Set up mouse event listeners
    const handleMouseDown = (event: MouseEvent) => onMouseDown(event);
    const handleMouseMove = (event: MouseEvent) => onMouseMove(event);
    const handleMouseUp = () => onMouseUp();

    canvasRefs.forEach((canvasRef) => {
      const canvas = canvasRef.current;
      canvas?.addEventListener("mousedown", handleMouseDown);
      canvas?.addEventListener("mousemove", handleMouseMove);
      canvas?.addEventListener("mouseup", handleMouseUp);
    });

    // startRenderingLoop();

    // Clean up event listeners on component unmount
    return () => {
      canvasRefs.forEach((canvasRef) => {
        const canvas = canvasRef.current;
        canvas?.removeEventListener("mousedown", handleMouseDown);
        canvas?.removeEventListener("mousemove", handleMouseMove);
        canvas?.removeEventListener("mouseup", handleMouseUp);
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
    controllingAircraftsRef.current = controllingAircrafts;  // controllingAircrafts が更新されたら、refも更新
  }, [controllingAircrafts]);

  useEffect(() => {
    isDisplayingRef.current = isDisplaying;
  }, [isDisplaying]);

  const updateCanvas = () => {
    if (!atsRouteData) {
      console.error("ATS Route data is missing or incomplete.");
      return;
    }

    const canvas = canvasRefs[bg]?.current;
    if (!canvas) {
      console.error(`Canvas element is not found for bg: ${bg}`);
      return;
    }

    const ctx = canvas.getContext("2d");
    if (ctx) {
      clearCanvas(ctx);
      renderMap(atsRouteData.waypoints, atsRouteData.radioNavigationAids, atsRouteData.atsLowerRoutes, atsRouteData.rnavRoutes, ctx, isDisplayingRef.current);
      
      const currentAircrafts = controllingAircraftsRef.current; // 最新の値を参照
      currentAircrafts.forEach((aircraft) => {
        DrawAircraft.drawAircraft(ctx, aircraft);
      });
      
      toggleCanvasDisplay();
    } else {
      console.error("Failed to get 2D context for canvas.");
    }
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
    for (const [index, aircraft] of currentControllingAircrafts.entries()) {
      const { position, label, callsign } = aircraft;
      const labelX = position.x + label.x;
      const labelY = position.y - label.y;

      if (isWithinRadius(x, y, position, aircraftRadius)) {
        console.log("Clicked on aircraft", callsign);
        changeDisplayCallsign(callsign);
        setSelectedAircraft(aircraft);
        break;
      }

      if (isWithinLabelBounds(x, y, labelX, labelY)) {
        console.log("Clicked on label", callsign);
        draggingLabelIndexRef.current = index;
        offsetXRef.current = x - labelX;
        offsetYRef.current = y - labelY;
        break;
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

  const changeDisplayCallsign = (newCallsign: string) => {
    const fontElement = document.getElementById("callsign") as HTMLParagraphElement;
    if (fontElement) {
      fontElement.textContent = newCallsign;
    }
  };

  // // useEffectで変更を監視してログを出力する方法
  // useEffect(() => {
  //   console.log("controllingAircrafts updated:", controllingAircrafts);
  // }, [controllingAircrafts]);

  const startUpdatingAircraftLocations = () => {
    const fetchLocationInterval = setInterval(async () => {
      try {
        const currentControllingAircrafts = controllingAircraftsRef.current;
        const updatedAircrafts = await fetchAircraftLocation(currentControllingAircrafts);
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
    <div className="radarArea relative flex-grow">
      <canvas ref={canvasRefs[0]} className="w-full h-full bg-black"></canvas>
      <canvas ref={canvasRefs[1]} className="hidden"></canvas>
    </div>
  );
};

export default RadarCanvas;