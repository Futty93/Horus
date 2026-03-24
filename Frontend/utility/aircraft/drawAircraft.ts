import CoordinateManager from "../coordinateManager/CoordinateManager";
import { GLOBAL_CONSTANTS } from "../globals/constants";
import { GLOBAL_SETTINGS } from "../globals/settings";
import { Aircraft } from "./aircraftClass";
import { DisplayRange } from "@/context/displayRangeContext";
import type { DataBlockDisplaySetting } from "@/context/dataBlockDisplaySettingContext";
import { formatEtaToUtcHhMm } from "./formatEtaUtc";

/**
 * Class to draw aircraft on the canvas
 * @example
 * import { DrawAircraft } from "./drawAircraft";
 * DrawAircraft.drawAircraft(ctx, aircraft);
 * @see Aircraft
 * @see CoordinateManager
 * @see GLOBAL_SETTINGS
 * @see GLOBAL_CONSTANTS
 * @see CanvasRenderingContext2D
 */
class DrawAircraft {
  public static drawAircraft(
    ctx: CanvasRenderingContext2D,
    aircraft: Aircraft,
    displayRange: DisplayRange,
    dataBlockDisplaySetting: DataBlockDisplaySetting
  ) {
    this.drawAircraftMarker(ctx, aircraft.position);
    this.drawHeadingLine(
      ctx,
      aircraft.position,
      aircraft.vector.groundSpeed,
      aircraft.vector.heading,
      displayRange
    );
    this.drawLabelLiine(ctx, aircraft.position, aircraft.label);
    this.drawAircraftLabel(ctx, aircraft, dataBlockDisplaySetting);
  }

  private static drawAircraftMarker(
    ctx: CanvasRenderingContext2D,
    position: { x: number; y: number }
  ) {
    const radius: number = 5;

    // Draw aircraft as a filled white circle
    ctx.beginPath();
    ctx.arc(position.x, position.y, radius, 0, 2 * Math.PI);
    ctx.fillStyle = "white";
    ctx.fill();
  }

  private static drawHeadingLine(
    ctx: CanvasRenderingContext2D,
    position: { x: number; y: number },
    groundSpeed: number,
    heading: number,
    displayRange: DisplayRange
  ) {
    const futurePosition = CoordinateManager.calculateFuturePositionOnCanvas(
      groundSpeed,
      heading,
      GLOBAL_SETTINGS.canvasWidth,
      GLOBAL_SETTINGS.canvasHeight,
      displayRange,
      position
    );

    // Draw a line from the current position to the future position
    ctx.beginPath();
    ctx.moveTo(position.x, position.y);
    ctx.lineTo(futurePosition.futureX, futurePosition.futureY);
    ctx.strokeStyle = "white";
    ctx.stroke();
  }

  private static drawAircraftLabel(
    ctx: CanvasRenderingContext2D,
    aircraft: Aircraft,
    setting: DataBlockDisplaySetting
  ) {
    const airplanePosition = aircraft.position;
    const instructedVector = aircraft.instructedVector;
    const labelX: number = airplanePosition.x + aircraft.label.x;
    let lineY: number = airplanePosition.y - aircraft.label.y;

    let altitudeLabel: string = "";
    if (instructedVector.altitude > airplanePosition.altitude) {
      altitudeLabel =
        Math.floor(instructedVector.altitude / 100).toString() +
        " ↑ " +
        Math.floor(airplanePosition.altitude / 100).toString();
    } else if (instructedVector.altitude < airplanePosition.altitude) {
      altitudeLabel =
        Math.floor(instructedVector.altitude / 100).toString() +
        " ↓ " +
        Math.floor(airplanePosition.altitude / 100).toString();
    } else {
      altitudeLabel = Math.floor(airplanePosition.altitude / 100).toString();
    }

    const riskLevel = aircraft.riskLevel || 0;
    let riskColor = "white";
    if (riskLevel >= 70) {
      riskColor = "red";
    } else if (riskLevel >= 30) {
      riskColor = "yellow";
    }

    ctx.fillStyle = "white";
    ctx.font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
    ctx.textAlign = "left";

    const lineHeight = 15;

    ctx.fillText(aircraft.callsign, labelX, lineY);
    lineY += lineHeight;

    ctx.fillText(altitudeLabel, labelX, lineY);
    lineY += lineHeight;

    ctx.fillText(
      "G" + Math.floor(aircraft.vector.groundSpeed / 10).toString(),
      labelX,
      lineY
    );
    ctx.fillText(
      aircraft.destinationIcao.length >= 4
        ? aircraft.destinationIcao.slice(-3)
        : aircraft.destinationIcao,
      labelX + 40,
      lineY
    );
    lineY += lineHeight;

    ctx.fillStyle = riskColor;
    ctx.fillText("R" + Math.floor(riskLevel).toString(), labelX, lineY);
    lineY += lineHeight;

    if (setting.aircraftType && aircraft.model) {
      ctx.fillStyle = "white";
      const modelDisplay =
        aircraft.model.length > 6 ? aircraft.model.slice(0, 6) : aircraft.model;
      ctx.fillText(modelDisplay, labelX, lineY);
      lineY += lineHeight;
    }

    if (setting.eta && aircraft.eta) {
      const etaFormatted = formatEtaToUtcHhMm(aircraft.eta);
      if (etaFormatted) {
        ctx.fillStyle = "white";
        ctx.fillText(etaFormatted, labelX, lineY);
        lineY += lineHeight;
      }
    }

    if (setting.squawk) {
      ctx.fillStyle = "white";
      const squawkDisplay =
        (aircraft as Aircraft & { squawk?: string }).squawk ?? "---";
      ctx.fillText(squawkDisplay, labelX, lineY);
    }
  }

  private static drawLabelLiine(
    ctx: CanvasRenderingContext2D,
    aircraftPosition: { x: number; y: number },
    labelPosition: { x: number; y: number }
  ) {
    const labelX: number = aircraftPosition.x + labelPosition.x;
    const labelY: number = aircraftPosition.y - labelPosition.y;
    const labelDistance: number = Math.sqrt(
      Math.pow(labelPosition.x, 2) + Math.pow(labelPosition.y, 2)
    );
    const sin = labelPosition.y / labelDistance;
    const cos = labelPosition.x / labelDistance;

    ctx.beginPath();
    ctx.moveTo(aircraftPosition.x + 10 * cos, aircraftPosition.y - 10 * sin);
    ctx.lineTo(labelX - 5, labelY + 15);
    ctx.strokeStyle = "white";
    ctx.stroke();
  }
}

export { DrawAircraft };
