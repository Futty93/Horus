// Define constants for canvas dimensions

import { Aircraft } from "./aircraft/aircraftClass.ts";
import { Waypoint } from "./AtsRouteManager/RouteInterfaces/Waypoint.ts";
import { CoordinateManager } from "./coordinateManager/CoordinateManager.ts";
import loadAtsRoutes from "./AtsRouteManager/atsRoutesLoader.ts";
import { renderMap } from "./AtsRouteManager/routeRenderer.ts";
import { GLOBAL_CONSTANTS } from "./globals/constants.ts";
import { GLOBAL_SETTINGS } from "./globals/settings.ts";
import { fetchAircraftLocation } from "./api/location.ts";
import { controlAircraft } from "./api/controlAircraft.ts";

/**
 * Represents the RadarGame class that encapsulates the game.
 */
class RadarGame {
  private updateInterval: any;
  private canvas: HTMLCanvasElement[]; //ダブルバッファで画面を切り替えてアニメーションを実現するために配列で定義
  private ctx: CanvasRenderingContext2D[];
  private clickedPosition: { x: number; y: number } | null; //クリックされた座標を取得
  private displayCallsign: HTMLDivElement;
  private inputAltitude: HTMLInputElement;
  private inputSpeed: HTMLInputElement;
  private inputHeading: HTMLInputElement;
  private confirmButton: HTMLInputElement;
  // private sendButton: HTMLInputElement;
  private inGame: boolean; //シミュレーションゲーム中かどうかを判断する
  private bg: number; //ダブルバッファの背景と表示を切り替えるためのインデックスを管理
  public controllingAircratfts: Aircraft[] = [];
  private displayingWaypoints: Waypoint[] = [];
  private draggingLabelIndex: number = -1; // Index of the label being dragged
  private offsetX: number = 0;
  private offsetY: number = 0;

  public selectedAircraft: Aircraft | null = null; //選択中の航空機を保持するための変数

  /**
   * Initializes a new instance of the RadarGame class.
   */
  constructor() {
    //初期変数を初期化する
    this.canvas = [this.createCanvas("radar"), this.createCanvas("radar2")];
    this.ctx = this.canvas.map((c) =>
      c.getContext("2d") as CanvasRenderingContext2D
    );
    this.clickedPosition = null;
    this.displayCallsign = <HTMLDivElement> document.getElementById("callsign");
    this.inputAltitude = <HTMLInputElement> document.getElementById("altitude");
    this.inputSpeed = <HTMLInputElement> document.getElementById("groundSpeed");
    this.inputHeading = <HTMLInputElement> document.getElementById("heading");
    this.confirmButton = <HTMLInputElement> document.getElementById(
      "confirmButton",
    );

    this.inGame = false;
    this.bg = 0;
    this.canvas[0].addEventListener("mousedown", (e) => this.onMouseDown(e));
    this.canvas[1].addEventListener("mousedown", (e) => this.onMouseDown(e));
    this.canvas[0].addEventListener("mousemove", (e) => this.onMouseMove(e));
    this.canvas[1].addEventListener("mousemove", (e) => this.onMouseMove(e));
    this.canvas[0].addEventListener("mouseup", () => this.onMouseUp());
    this.canvas[1].addEventListener("mouseup", () => this.onMouseUp());

    this.initializeAtsRouteData();

  }

  public atsRouteData: any;

  private async initializeAtsRouteData() {
    this.atsRouteData = await loadAtsRoutes();
  }

  /**
   * Creates a canvas element with the specified id.
   * @param id - The id of the canvas element.
   * @returns The created HTMLCanvasElement.
   */
  private createCanvas(id: string): HTMLCanvasElement {
    //HTMLからキャンバスを取得してきて、もし取得できなければエラーを出力
    const canvas = document.getElementById(id) as HTMLCanvasElement;
    if (!canvas) {
      throw new Error(`Canvas element with id "${id}" not found.`);
    }
    return canvas;
  }

  /**
   * Clears the canvas at the specified index.
   * @param index - The index of the canvas to clear.
   */
  private clearCanvas(index: number): void {
    //ダブルバッファで新しい画面を描画する前に一旦消す
    this.ctx[index].fillStyle = "black";
    this.ctx[index].fillRect(0, 0, GLOBAL_SETTINGS.canvasWidth, GLOBAL_SETTINGS.canvasHeight);
  }

  /**
   * Gets a mouse click event and returns the aircraft closest to the clicked location.
   * @param event - The mouse click event.
   */
  private onMouseDown(event: MouseEvent): void {
    // Get the clicked position relative to the canvas
    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const aircraftRadius = 30; // Adjust the radius as needed

    // Iterate through the aircraft to check if they are near the clicked position
    for (const [index, airplane] of this.controllingAircratfts.entries()) {
      const { position, label, callsign } = airplane;
      const labelX = position.x + label.x;
      const labelY = position.y - label.y;

      // Check if the clicked position is near the aircraft's position
      if (this.isWithinRadius(x, y, position, aircraftRadius)) {
        this.changeDisplayCallsign(callsign);
        this.inputAltitude.value = airplane.instructedVector.altitude.toString();
        this.inputSpeed.value = airplane.instructedVector.groundSpeed.toString();
        this.inputHeading.value = airplane.instructedVector.heading.toString();
        this.selectedAircraft = airplane;
        break; // Exit loop once an aircraft is found
      }

      // Check if the label was clicked
      if (this.isWithinLabelBounds(x, y, labelX, labelY)) {
        this.draggingLabelIndex = index;
        this.offsetX = x - labelX;
        this.offsetY = y - labelY;
        break; // Exit loop once a label is found
      }
    }
  }

  /**
   * Checks if the click is within the radius of the aircraft position.
   * @param x - Clicked x-coordinate.
   * @param y - Clicked y-coordinate.
   * @param position - Aircraft position.
   * @param radius - Radius to check within.
   * @returns True if the click is within the radius, otherwise false.
   */
  private isWithinRadius(
    x: number,
    y: number,
    position: { x: number; y: number },
    radius: number,
  ): boolean {
    return (
      x >= position.x - radius &&
      x <= position.x + radius &&
      y >= position.y - radius &&
      y <= position.y + radius
    );
  }

  /**
   * Checks if the click is within the bounds of the label.
   * @param x - Clicked x-coordinate.
   * @param y - Clicked y-coordinate.
   * @param labelX - Label x-coordinate.
   * @param labelY - Label y-coordinate.
   * @returns True if the click is within the label bounds, otherwise false.
   */
  private isWithinLabelBounds(
    x: number,
    y: number,
    labelX: number,
    labelY: number,
  ): boolean {
    return (
      x >= labelX - 5 &&
      x <= labelX + 70 &&
      y >= labelY - 20 &&
      y <= labelY + 40
    );
  }

  /**
   * Handles the mouse move event to update the position of a dragged label.
   * @param event - The mouse move event.
   */
  private onMouseMove(event: MouseEvent): void {
    if (this.draggingLabelIndex === -1) return; // No label is being dragged

    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    const aircraft = this.controllingAircratfts[this.draggingLabelIndex];
    const label = aircraft.label;
    const aircraftPosition = aircraft.position;

    // Update the label position based on mouse movement
    label.x = mouseX - this.offsetX - aircraftPosition.x;
    label.y = aircraftPosition.y - (mouseY - this.offsetY);
  }

  /**
   * Handles the mouse up event.
   */
  private onMouseUp(): void {
    this.draggingLabelIndex = -1; // Reset dragging label index
  }

  /**
   * Changes the displayed callsign.
   * @param newCallsign - The new callsign to display.
   */
  private changeDisplayCallsign(newCallsign: string): void {
    const fontElement = <HTMLParagraphElement> document.getElementById(
      "callsign",
    );
    if (fontElement) {
      fontElement.textContent = newCallsign;
    }
  }

  /**
   * Draws the airplane representation on the canvas.
   * @param index - The index of the airplane in the list.
   * @param airplane - The instance of the Airplane class.
   */
  private drawAirplane(index: number, airplane: Aircraft): void {
    const position = airplane.position;
    const radius: number = 5;

    // Draw airplane as a filled white circle
    this.ctx[index].beginPath();
    this.ctx[index].arc(
      position.x,
      position.y,
      radius,
      0,
      2 * Math.PI,
    );
    this.ctx[index].fillStyle = "white";
    this.ctx[index].fill();
  }

  /**
   * Draws the heading line representing the airplane's movement direction.
   * @param index - The index of the airplane in the list.
   * @param airplane - The instance of the Aircraft class.
   */
  private drawHeadingLine(index: number, airplane: Aircraft): void {
    const { x: startX, y: startY } = airplane.position;
    const { groundSpeed, heading } = airplane.vector;

    // Calculate the future position of the airplane on the canvas
    const futurePosition = CoordinateManager
      .calculateFuturePositionOnCanvas(
        groundSpeed,
        heading,
        GLOBAL_SETTINGS.canvasWidth,
        GLOBAL_SETTINGS.canvasHeight,
        GLOBAL_SETTINGS.displayRange,
        airplane.position,
      );

    // Draw the heading line
    this.ctx[index].beginPath();
    this.ctx[index].moveTo(startX, startY);
    this.ctx[index].lineTo(futurePosition.futureX, futurePosition.futureY);
    this.ctx[index].strokeStyle = "white";
    this.ctx[index].stroke();
  }

  /**
   * Draws the line connecting airplane to its label position.
   * @param index - The index of the airplane in the list.
   * @param airplane - The instance of the Airplane class.
   */
  private drawLabelLine(index: number, airplane: Aircraft): void {
    const position = airplane.position;
    const labelX: number = position.x + airplane.label.x;
    const labelY: number = position.y - airplane.label.y;
    const labelDistance = Math.sqrt(
      Math.pow(airplane.label.x, 2) +
        Math.pow(airplane.label.y, 2),
    );
    const sin = airplane.label.y / labelDistance;
    const cos = airplane.label.x / labelDistance;

    // Draw a line connecting airplane to its label
    this.ctx[index].beginPath();
    this.ctx[index].moveTo(
      position.x + (10 * cos),
      position.y - (10 * sin),
    );
    this.ctx[index].lineTo(labelX - 5, labelY + 15);
    this.ctx[index].strokeStyle = "white";
    this.ctx[index].stroke();
  }

  /**
   * Draw the label containing airplane information.
   * @param index - Index of the airplane in the list.
   * @param airplane - Instance of the Airplane class.
   */
  private drawLabel(index: number, airplane: Aircraft): void {
    const airplanePosition = airplane.position;
    const labelX: number = airplanePosition.x +
      airplane.label.x;
    const labelY: number = airplanePosition.y -
      airplane.label.y;

    // Draw labels with airplane information
    this.ctx[index].fillStyle = "white";
    this.ctx[index].font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
    this.ctx[index].textAlign = "left";

    this.ctx[index].fillText(airplane.callsign, labelX, labelY);
    this.ctx[index].fillText(
      Math.floor(airplanePosition.altitude / 100).toString(),
      labelX,
      labelY + 15,
    );
    this.ctx[index].fillText(
      "G" + (Math.floor(airplane.vector.groundSpeed / 10)).toString(),
      labelX,
      labelY + 30,
    );
    this.ctx[index].fillText(
      airplane.destinationIata,
      labelX + 40,
      labelY + 30,
    );
  }

  /**
   * Draw the airplane details on the canvas.
   * @param index - Index of the airplane in the list.
   * @param airplane - Instance of the Airplane class.
   */
  public drawAirplaneDetails(index: number, airplane: Aircraft): void {
    // Draw airplane representation, heading line, label line, and label
    this.drawAirplane(index, airplane);
    this.drawHeadingLine(index, airplane);
    this.drawLabelLine(index, airplane);
    this.drawLabel(index, airplane);
  }

  private toggleCanvasDisplay(): void {
    //ダブルバッファの表示するキャンバスを切り替える
    this.canvas[1 - this.bg].style.display = "none";
    this.canvas[this.bg].style.display = "block";
    this.bg = 1 - this.bg;
  }

  private async update(): Promise<void> {
    //画面全体を更新する
    this.clearCanvas(this.bg);

    renderMap(this.atsRouteData.waypoints, this.atsRouteData.radioNavigationAids, this.atsRouteData.atsLowerRoutes, this.atsRouteData.rnavRoutes, this.ctx[this.bg]);

    for (let i = 0; i < this.controllingAircratfts.length; i++) {
      // this.updatePosition(this.controlledAirplane[i]);
      this.drawAirplaneDetails(this.bg, this.controllingAircratfts[i]);
    }
    this.toggleCanvasDisplay();
  }

  public async makeFirstCanvas(): Promise<void> {
    if (!this.ctx[0] || !this.ctx[1]) {
      console.error("Failed to get 2D context");
      return;
    }
  }

  public async start(): Promise<void> {
    this.updateInterval = setInterval(() => {
      this.update();
    }, 1000 / GLOBAL_CONSTANTS.REFRESH_RATE);
  }

  public stop(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
      console.log("Game stopped");
    }
  }
}

// Initialize and start the game
const radarGame = new RadarGame();

// when the page is loaded, make the first canvas
radarGame.makeFirstCanvas();

loadAtsRoutes().then((data) => {
  console.log(data);
}).catch((error) => {
  console.error(error);
});

// when the start button is clicked, start the game and start fetching aircraft locations
const startButton = document.getElementById("startButton");
startButton?.addEventListener("click", () => {
  radarGame.start();

  console.log("Game started");
  // Start fetching aircraft location every 1 second
  if (!GLOBAL_SETTINGS.locationUpdateInterval) {
    // GLOBAL_SETTINGS.locationUpdateInterval = setInterval(fetchAircraftLocation, 1000); // 1 second interval
    GLOBAL_SETTINGS.locationUpdateInterval = setInterval(async () => {
      radarGame.controllingAircratfts = await fetchAircraftLocation(radarGame.controllingAircratfts);
    }, 1000); // 1 second interval
  }
});

// when the stop button is clicked, stop the game and stop fetching aircraft locations
const stopButton = document.getElementById("stopButton");
stopButton?.addEventListener("click", () => {
  radarGame.stop();

  // Stop fetching aircraft location
  if (GLOBAL_SETTINGS.locationUpdateInterval) {
    clearInterval(GLOBAL_SETTINGS.locationUpdateInterval);
    GLOBAL_SETTINGS.locationUpdateInterval = null;
  }
});

//* when the confirm button is clicked, control the selected aircraft
const confirmButton = document.getElementById("confirmButton");
confirmButton?.addEventListener("click", () => {
  if (radarGame.selectedAircraft) {
    console.log("Confirm button clicked");
    controlAircraft(radarGame.selectedAircraft.callsign);
  } else {
    console.error("No aircraft selected");
  }
});

const displayRangeElement = document.getElementById("displayRange");
displayRangeElement?.addEventListener("input", (event) => {
  const newRange = parseFloat((event.target as HTMLInputElement).value);
  GLOBAL_SETTINGS.displayRange = newRange;
});

// Resetボタンの参照を取得
const resetButton = document.getElementById("resetButton");

// ボタンがクリックされたときにAPIリクエストを送信する関数
const handleResetButtonClick = async () => {
  console.log("Reset button clicked");
  try {
    const response = await fetch("http://localhost:8080/hello/", {
      method: "GET",
      headers: {
        "accept": "*/*",
      },
    });

    if (response.ok) {
      const data = await response.text(); // Fetches text data
      console.log("Response Data:", data);
    } else {
      console.error("Request failed with status:", response.status);
    }
  } catch (error) {
    console.error("Error occurred:", error);
  }
};

// ボタンにクリックイベントリスナーを追加
resetButton?.addEventListener("click", handleResetButtonClick);


// マッピングオブジェクトを作成
const settingsMap = {
  "waypoint-name": "isDisplayingWaypointName",
  "waypoint-point": "isDisplayingWaypointPoint",
  "radio-navigation-aids-name": "isDisplayingRadioNavigationAidsName",
  "radio-navigation-aids-point": "isDisplayingRadioNavigationAidsPoint",
  "ats-lower-routes": "isDisplayingAtsLowerRoute",
  "rnav-routes": "isDisplayingRnavRoute",
};

// 共通のイベントハンドラ
function handleCheckboxChange(settingKey: string) {
  return (event: Event) => {
    const isChecked = (event.target as HTMLInputElement).checked;
    GLOBAL_SETTINGS[settingKey] = isChecked;
  };
}

// マッピングに従ってイベントリスナーを追加
Object.keys(settingsMap).forEach((checkboxId) => {
  const checkBoxElement = document.getElementById(checkboxId);
  const settingKey = settingsMap[checkboxId];
  
  checkBoxElement?.addEventListener("change", handleCheckboxChange(settingKey));
});

const sectorCenterCoordinates = {
  "T09": { latitude: 34.482083333333335 , longitude: 138.61388888888888 },
  "T10": { latitude: 33.04138888888889 , longitude: 139.4561111111111 },
  "T14": { latitude: 33.66722222222222 , longitude: 137.91833333333335 },
  "T25": { latitude: 34.54944444444445 , longitude: 136.96555555555557 },
  "T30": { latitude: 43.29444444444445 , longitude: 142.67916666666667 },
  "T31": { latitude: 41.85805555555555 , longitude: 140.1590277777778 },
  "T32": { latitude: 40.209722222222226 , longitude: 141.23722222222221 },
  "T33": { latitude: 38.474999999999994 , longitude: 138.8048611111111 },
  "T34": { latitude: 37.543055555555554 , longitude: 141.55124999999998 },
  "T35": { latitude: 36.84736111111111 , longitude: 139.40930555555553 },
  "T36": { latitude: 35.77388888888889 , longitude: 142.14041666666665 },
  "T38": { latitude: 35.827083333333334 , longitude: 139.15763888888887 },
  "T39": { latitude: 35.273472222222225 , longitude: 139.24930555555557 },
  "T45": { latitude: 34.30236111111111 , longitude: 135.53097222222223 },
  "T46": { latitude: 33.43291666666667 , longitude: 135.91680555555556 },
  "T92": { latitude: 38.30972222222222 , longitude: 141.12583333333333 },
  "T93": { latitude: 36.28833333333333 , longitude: 142.9763888888889 },
  "F01": { latitude: 42.377361111111114 , longitude: 141.8722222222222 },
  "F04": { latitude: 39.608194444444436 , longitude: 136.90722222222223 },
  "F05": { latitude: 35.63972222222222 , longitude: 138.83125 },
  "F07": { latitude: 36.840694444444445 , longitude: 135.81527777777777 },
  "F08": { latitude: 35.93208333333333 , longitude: 132.54180555555553 },
  "F09": { latitude: 33.681805555555556 , longitude: 134.32638888888889 },
  "F10": { latitude: 32.59625 , longitude: 134.68847222222223 },
  "F11": { latitude: 31.687083333333334 , longitude: 135.36013888888888 },
  "F12": { latitude: 33.90861111111111 , longitude: 131.05680555555557 },
  "F13": { latitude: 31.779722222222222 , longitude: 127.51916666666668 },
  "F14": { latitude: 31.530277777777776 , longitude: 130.745 },
  "F15": { latitude: 28.71666666666667 , longitude: 126.49805555555555 },
  "F16": { latitude: 28.601805555555558 , longitude: 129.5948611111111 },
  "F17": { latitude: 24.36763888888889 , longitude: 126.57083333333333 },
  "N43": { latitude: 36.96819444444444 , longitude: 137.32430555555555 },
  "N44": { latitude: 35.09638888888889 , longitude: 136.40041666666667 },
  "N47": { latitude: 35.17722222222223 , longitude: 135.38680555555555 },
  "N48": { latitude: 36.169444444444444 , longitude: 134.56944444444446 },
  "N49": { latitude: 34.59513888888889 , longitude: 134.17916666666667 },
  "N50": { latitude: 33.25541666666667 , longitude: 133.79736111111112 },
  "N51": { latitude: 34.531111111111116 , longitude: 131.56319444444443 },
  "N52": { latitude: 33.38305555555556 , longitude: 132.2573611111111 },
  "N53": { latitude: 33.26736111111111 , longitude: 129.81375000000003 },
  "N54": { latitude: 31.617222222222225 , longitude: 131.49763888888887 },
  "N55": { latitude: 28.23111111111111 , longitude: 128.70791666666668 },
  "A01": { latitude: 44.36263888888889 , longitude: 151.79333333333335 },
  "A02": { latitude: 42.24486111111111 , longitude: 154.02319444444444 },
  "A03": { latitude: 35.25888888888889 , longitude: 153.5966666666667 },
  "A04": { latitude: 27.654166666666665 , longitude: 143.64499999999998 },
  "A05": { latitude: 24.915694444444444 , longitude: 132.88125 }
};

const sectorSelecter = document.getElementById("selectSector");
sectorSelecter?.addEventListener("change", (event) => {
  const selectedSector = (event.target as HTMLSelectElement).value;
  console.log("Selected sector:", selectedSector);
  GLOBAL_SETTINGS.centerCoordinates = sectorCenterCoordinates[selectedSector];
});