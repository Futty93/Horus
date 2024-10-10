import CoordinateManager from "../coordinateManager/CoordinateManager";
import { GLOBAL_CONSTANTS } from "../globals/constants";
import { GLOBAL_SETTINGS } from "../globals/settings";
import { Aircraft } from "./aircraftClass";


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
  public static drawAircraft(ctx: CanvasRenderingContext2D, aircraft: Aircraft) {
    this.drawAircraftMarker(ctx, aircraft.position);
    this.drawHeadingLine(ctx, aircraft.position, aircraft.vector.groundSpeed, aircraft.vector.heading);
    this.drawLabelLiine(ctx, aircraft.position, aircraft.label, aircraft.highlightRank);
    this.drawAircraftLabel(ctx, aircraft);
  }

  private static drawAircraftMarker(ctx: CanvasRenderingContext2D, position: { x: number; y: number }) {
    const radius: number = 5;

    // Draw aircraft as a filled white circle
    ctx.beginPath();
    ctx.arc(
      position.x,
      position.y,
      radius,
      0,
      2 * Math.PI,
    );
    ctx.fillStyle = "white";
    ctx.fill();
  }

  private static drawHeadingLine(ctx: CanvasRenderingContext2D, position: { x: number; y: number }, groundSpeed: number, heading: number) {
    const futurePosition = CoordinateManager.calculateFuturePositionOnCanvas(
      groundSpeed,
      heading,
      GLOBAL_SETTINGS.canvasWidth,
      GLOBAL_SETTINGS.canvasHeight,
      GLOBAL_SETTINGS.displayRange,
      position,
    );

    // Draw a line from the current position to the future position
    ctx.beginPath();
    ctx.moveTo(position.x, position.y);
    ctx.lineTo(futurePosition.futureX, futurePosition.futureY);
    ctx.strokeStyle = "white";
    ctx.stroke();
  }

  private static drawAircraftLabel(ctx: CanvasRenderingContext2D, aircraft: Aircraft) {
    const airplanePosition = aircraft.position;
    const labelX: number = airplanePosition.x + aircraft.label.x;
    const labelY: number = airplanePosition.y - aircraft.label.y;

    // Draw labels with airplane information
    ctx.fillStyle = "white";
    ctx.font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
    ctx.textAlign = "left";

    ctx.fillText(aircraft.callsign, labelX, labelY);
    ctx.fillText(
      Math.floor(airplanePosition.altitude / 100).toString(),
      labelX,
      labelY + 15,
    );
    ctx.fillText(
      "G" + (Math.floor(aircraft.vector.groundSpeed / 10)).toString(),
      labelX,
      labelY + 30,
    );
    ctx.fillText(
      aircraft.destinationIata,
      labelX + 40,
      labelY + 30,
    );
  }

  private static drawLabelLiine(ctx: CanvasRenderingContext2D, aircraftPosition: { x: number; y: number }, labelPosition: { x: number; y: number }, highlightRank: number) {
    const labelX: number = aircraftPosition.x + labelPosition.x;
    const labelY: number = aircraftPosition.y - labelPosition.y;
    const labelDistance: number = Math.sqrt(Math.pow(labelPosition.x, 2) + Math.pow(labelPosition.y, 2));
    const sin = labelPosition.y / labelDistance;
    const cos = labelPosition.x / labelDistance;

    ctx.beginPath();
    ctx.moveTo(aircraftPosition.x + (10 * cos), aircraftPosition.y - (10 * sin));
    ctx.lineTo(labelX - 5, labelY + 15);
    ctx.strokeStyle = "white";
    ctx.stroke();

    // コールサイン等のラベルの背景に色をつける
    if ( highlightRank == 0 ) {
      return;
    }

    const cornerRadius = 5;
    const labelWidth = 75;  // 70 + 5 for padding
    const labelHeight = 60; // 40 + 20 for padding

    ctx.beginPath();
    ctx.moveTo(labelX - 5 + cornerRadius, labelY - 20);

    // Top Line with top-right corner
    ctx.lineTo(labelX - 5 + labelWidth - cornerRadius, labelY - 20);
    ctx.arcTo(labelX - 5 + labelWidth, labelY - 20, labelX - 5 + labelWidth, labelY - 20 + cornerRadius, cornerRadius);

    // Right Line with bottom-right corner
    ctx.lineTo(labelX - 5 + labelWidth, labelY + 40 - cornerRadius);
    ctx.arcTo(labelX - 5 + labelWidth, labelY + 40, labelX - 5 + labelWidth - cornerRadius, labelY + 40, cornerRadius);

    // Bottom Line with bottom-left corner
    ctx.lineTo(labelX - 5 + cornerRadius, labelY + 40);
    ctx.arcTo(labelX - 5, labelY + 40, labelX - 5, labelY + 40 - cornerRadius, cornerRadius);

    // Left Line with top-left corner
    ctx.lineTo(labelX - 5, labelY - 20 + cornerRadius);
    ctx.arcTo(labelX - 5, labelY - 20, labelX - 5 + cornerRadius, labelY - 20, cornerRadius);

    ctx.closePath();

    // Fill with light green color
    if ( highlightRank == 1 ) {
      ctx.fillStyle = "#00FFFF";
    } else if ( highlightRank == 2 ) {
      ctx.fillStyle = "#00FF00";
    }
    ctx.fill();
    
  }
}

export { DrawAircraft };