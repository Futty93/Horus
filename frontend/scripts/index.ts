// Define constants for canvas dimensions
const CANVAS_WIDTH: number = 1000;
const CANVAS_HEIGHT: number = 1000;
const REFRESH_RATE: number = 60; //画面の更新頻度(fps)

import { Aircraft } from "./aircraftClass.ts";
import { Waypoint } from "./waypointManager.ts";
import { WaypointManager } from "./waypointManager.ts";
import { CoordinateManager } from "./CoordinateManager.ts";

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
  private waypointManager: WaypointManager;
  public coordinateManager: CoordinateManager;
  // private sendButton: HTMLInputElement;
  private inGame: boolean; //シミュレーションゲーム中かどうかを判断する
  private bg: number; //ダブルバッファの背景と表示を切り替えるためのインデックスを管理
  public controlledAirplane: Aircraft[] = [];
  private displayingWaypoints: Waypoint[] = [];
  private draggingLabelIndex: number = -1; // Index of the label being dragged
  private offsetX: number = 0;
  private offsetY: number = 0;

  public centerCoordinates = {
    latitude: 35.54823,
    longitude: 139.77795,
  };
  public displayRange: number;

  private selectedAircraft: Aircraft | null = null; //選択中の航空機を保持するための変数

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
    this.inputSpeed = <HTMLInputElement> document.getElementById("speed");
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
    this.confirmButton.addEventListener("click", () => this.send_command());

    this.waypointManager = new WaypointManager();
    this.coordinateManager = new CoordinateManager(CANVAS_WIDTH, CANVAS_HEIGHT);

    this.displayRange = 100; // Default display range in kilometers

    // //とりあえず10機の航空機を生成する
    // for (let i = 1; i <= 10; i++) {
    //   const airplane = new Airplane();
    //   this.controlledAirplane.push(airplane);
    // }
  }
  send_command(): any {
    throw new Error("Method not implemented.");
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
    this.ctx[index].fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  }

  // /**
  //  * Updates the position of the specified airplane.
  //  * @param airplane - The airplane to update the position for.
  //  */
  // private updatePosition(airplane: Airplane): void {
  //   //航空機の速度に合わせてポジションを更新する
  //   airplane.updateLocation(REFRESH_RATE);
  // }

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
    for (const [index, airplane] of this.controlledAirplane.entries()) {
      const { position, label, callsign } = airplane;
      const labelX = position.x + label.x;
      const labelY = position.y - label.y;

      // Check if the clicked position is near the aircraft's position
      if (this.isWithinRadius(x, y, position, aircraftRadius)) {
        this.changeDisplayCallsign(callsign);
        this.inputAltitude.value = airplane.position.altitude.toString();
        this.inputSpeed.value = airplane.vector.groundSpeed.toString();
        this.inputHeading.value = airplane.vector.heading.toString();
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

    const aircraft = this.controlledAirplane[this.draggingLabelIndex];
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
  const futurePosition = this.coordinateManager.calculateFuturePositionOnCanvas(
    groundSpeed,
    heading,
    CANVAS_WIDTH,
    CANVAS_HEIGHT,
    this.displayRange,
    airplane.position
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

    // コールサイン等のラベルの背景に色をつける
    // if ( airplaneInfo.heading == '26' ) {
    //   //ラベルの周りを線で囲いたいとき↓↓
    //   // this.ctx[index].beginPath();
    //   // this.ctx[index].strokeStyle = "white";
    //   // this.ctx[index].lineWidth = 2;
    //   // this.ctx[index].moveTo(labelX-5, labelY-20); // Move to the starting point
    //   // this.ctx[index].lineTo(labelX + 70, labelY-20); // Draw top line
    //   // this.ctx[index].lineTo(labelX + 70, labelY + 40); // Draw right line
    //   // this.ctx[index].lineTo(labelX-5, labelY + 40); // Draw bottom line
    //   // this.ctx[index].lineTo(labelX-5, labelY-20); // Draw left line to close the rectangle
    //   // this.ctx[index].stroke(); // Stroke the path
    //   const cornerRadius = 5;
    //   const labelWidth = 75;  // 70 + 5 for padding
    //   const labelHeight = 60; // 40 + 20 for padding

    //   this.ctx[index].beginPath();
    //   this.ctx[index].moveTo(labelX - 5 + cornerRadius, labelY - 20);

    //   // Top Line with top-right corner
    //   this.ctx[index].lineTo(labelX - 5 + labelWidth - cornerRadius, labelY - 20);
    //   this.ctx[index].arcTo(labelX - 5 + labelWidth, labelY - 20, labelX - 5 + labelWidth, labelY - 20 + cornerRadius, cornerRadius);

    //   // Right Line with bottom-right corner
    //   this.ctx[index].lineTo(labelX - 5 + labelWidth, labelY + 40 - cornerRadius);
    //   this.ctx[index].arcTo(labelX - 5 + labelWidth, labelY + 40, labelX - 5 + labelWidth - cornerRadius, labelY + 40, cornerRadius);

    //   // Bottom Line with bottom-left corner
    //   this.ctx[index].lineTo(labelX - 5 + cornerRadius, labelY + 40);
    //   this.ctx[index].arcTo(labelX - 5, labelY + 40, labelX - 5, labelY + 40 - cornerRadius, cornerRadius);

    //   // Left Line with top-left corner
    //   this.ctx[index].lineTo(labelX - 5, labelY - 20 + cornerRadius);
    //   this.ctx[index].arcTo(labelX - 5, labelY - 20, labelX - 5 + cornerRadius, labelY - 20, cornerRadius);

    //   this.ctx[index].closePath();

    //   // Fill with light green color
    //   this.ctx[index].fillStyle = "#008000";
    //   this.ctx[index].fill();

    //   // // Draw the border with white color
    //   // this.ctx[index].strokeStyle = "white";
    //   // this.ctx[index].lineWidth = 2;
    //   // this.ctx[index].stroke();
    // }
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
    this.ctx[index].font = "12px Arial";
    this.ctx[index].textAlign = "left";

    this.ctx[index].fillText(airplane.callsign, labelX, labelY);
    this.ctx[index].fillText(airplanePosition.altitude.toString(), labelX, labelY + 15);
    this.ctx[index].fillText(airplane.vector.groundSpeed.toString(), labelX, labelY + 30);
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

  public drawWaypoint(
    index: number,
    name: string,
    latitude: number,
    longitude: number,
  ): void {
    const radius: number = 5;
    const sides: number = 5; // 5角形
    const angle: number = (2 * Math.PI) / sides;

    this.ctx[index].beginPath();
    for (let i = 0; i <= sides; i++) {
      const x = latitude + radius * Math.cos(i * angle);
      const y = longitude + radius * Math.sin(i * angle);
      if (i === 0) {
        this.ctx[index].moveTo(x, y);
      } else {
        this.ctx[index].lineTo(x, y);
      }
    }
    this.ctx[index].closePath();
    this.ctx[index].strokeStyle = "gray";
    this.ctx[index].stroke();

    // 五角形の右上にテキストを描画
    const textX = latitude + radius * Math.cos(angle / 2); // 右上の頂点のX座標
    const textY = longitude - radius; // 五角形の上の方に少し離す
    this.ctx[index].fillStyle = "gray";
    this.ctx[index].font = "12px Arial";
    this.ctx[index].fillText(name, textX, textY);
  }

  private toggleCanvasDisplay(): void {
    //ダブルバッファの表示するキャンバスを切り替える
    this.canvas[1 - this.bg].style.display = "none";
    this.canvas[this.bg].style.display = "block";
    this.bg = 1 - this.bg;
  }

  // private send_command(): void {
  //   if (this.selectedAircraft) {
  //     const { newAltitude, newSpeed, newHeading } = this.selectedAircraft
  //       .changeCommandedInfo(
  //         Number(this.inputAltitude.value),
  //         Number(this.inputSpeed.value),
  //         Number(this.inputHeading.value),
  //       );

  //     // Now you have the extracted values in newAltitude, newSpeed, and newHeading
  //     this.inputAltitude.value = newAltitude;
  //     this.inputSpeed.value = newSpeed;
  //     this.inputHeading.value = newHeading;
  //   }
  //   console.log(this.selectedAircraft);
  // }

  private update(): void {
    //画面全体を更新する
    this.clearCanvas(this.bg);
    for (let i = 0; i < this.displayingWaypoints.length; i++) {
      this.drawWaypoint(
        this.bg,
        this.displayingWaypoints[i].name,
        this.displayingWaypoints[i].latitude,
        this.displayingWaypoints[i].longitude,
      );
    }
    for (let i = 0; i < this.controlledAirplane.length; i++) {
      // this.updatePosition(this.controlledAirplane[i]);
      this.drawAirplaneDetails(this.bg, this.controlledAirplane[i]);
    }
    this.toggleCanvasDisplay();
  }

  public updateWaypoints(): void {
    // Update the waypoints based on the center coordinates and display range
    this.waypointManager.updateFilteredWaypoints(
      this.centerCoordinates,
      this.displayRange,
    );
    this.displayingWaypoints = this.waypointManager.getFilteredWaypoints();
    // Calculate the canvas coordinates for the waypoints
    for (let i = 0; i < this.displayingWaypoints.length; i++) {
      const { x, y } = this.coordinateManager.calculateCanvasCoordinates(
        this.centerCoordinates.latitude,
        this.centerCoordinates.longitude,
        this.displayRange,
        this.displayingWaypoints[i].latitude,
        this.displayingWaypoints[i].longitude,
      );
      this.displayingWaypoints[i].latitude = x;
      this.displayingWaypoints[i].longitude = y;
    }
    this.update(); // Update the display with new waypoints
  }

  public async makeFirstCanvas(): Promise<void> {
    if (!this.ctx[0] || !this.ctx[1]) {
      console.error("Failed to get 2D context");
      return;
    }
    await this.waypointManager.loadWaypoints();
    this.updateWaypoints();
  }

  public async start(): Promise<void> {
    this.updateInterval = setInterval(() => {
      this.update();
    }, 1000 / REFRESH_RATE);
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

// Timer variable to hold interval reference
let locationUpdateInterval: number | null = null;

const fetchAircraftLocation = async () => {
  try {
    const response = await fetch("http://localhost:8080/aircraft/location/all", {
      method: "GET",
      headers: {
        "accept": "*/*", // Assuming the server sends a custom format
      },
    });

    if (response.ok) {
      const textData = await response.text(); // Fetches text data
      console.log("Raw Aircraft Locations:", textData);

      // Example: Parsing custom format (e.g., CommercialAircraft{callsign=...})
      const aircraftData = parseAircraftData(textData);
      if (aircraftData) {
        updateControlledAirplanes(aircraftData); // Call the function to update controlledAirplanes
      } else {
        console.error("Failed to parse aircraft data");
      }
    } else {
      console.error("Request failed with status:", response.status);
    }
  } catch (error) {
    console.error("Error occurred while fetching aircraft location:", error);
  }
};

// Function to parse custom format into an array of objects
const parseAircraftData = (data: string): Aircraft[] | null => {
  // Implement parsing logic based on the actual format of your data
  // This is a placeholder example; you'll need to adjust it according to the actual format
  try {
    // Example parsing logic (assuming data is in some custom text format)
    const aircraftStrings = data.split('\n').filter(line => line.startsWith('CommercialAircraft'));
    return aircraftStrings.map(aircraftString => {
      // Parse each aircraftString into an Aircraft object
      // Example: Implement a function to parse the string into a valid Aircraft object
      return parseAircraftString(aircraftString);
    });
  } catch (error) {
    console.error("Error parsing aircraft data:", error);
    return null;
  }
};

// Function to parse a single aircraft string into an Aircraft object
const parseAircraftString = (aircraftString: string): Aircraft => {
  // Implement the actual parsing logic based on your data format
  // This is a placeholder; adjust it according to your format
  // Example: Extract fields from the string and return a new Aircraft object
  const matches = aircraftString.match(/callsign=(.*?), position=\{latitude=(.*?), longitude=(.*?), altitude=(.*?)\}, vector=\{heading=(.*?), groundSpeed=(.*?), verticalSpeed=(.*?)\}, type=(.*?), originIata=(.*?), originIcao=(.*?), destinationIata=(.*?), destinationIcao=(.*?), eta=(.*?)\}/);
  if (matches) {
    const coordinateOnCanvas = radarGame.coordinateManager.calculateCanvasCoordinates(radarGame.centerCoordinates.latitude, radarGame.centerCoordinates.longitude, radarGame.displayRange, parseFloat(matches[2]), parseFloat(matches[3]));
    return new Aircraft(
      matches[1], // callsign
      { x: coordinateOnCanvas.x, y: coordinateOnCanvas.y, altitude: parseFloat(matches[4]) }, // position
      { heading: parseFloat(matches[5]), groundSpeed: parseFloat(matches[6]), verticalSpeed: parseFloat(matches[7]) }, // vector
      matches[8], // type
      matches[9], // originIata
      matches[10], // originIcao
      matches[11], // destinationIata
      matches[12], // destinationIcao
      matches[10] // eta
    );
  }
  throw new Error("Failed to parse aircraft string: " + aircraftString);
};

// Function to update controlledAirplanes based on API data (from earlier code)
function updateControlledAirplanes(apiResponse: Aircraft[]) {
  const newAircraftMap = new Map<string, Aircraft>();

  apiResponse.forEach((aircraft) => {
    newAircraftMap.set(aircraft.callsign, aircraft);
  });

  radarGame.controlledAirplane = radarGame.controlledAirplane.filter(
    (airplane) => {
      const newAircraft = newAircraftMap.get(airplane.callsign);
      if (newAircraft) {
        airplane.updateAircraftInfo(newAircraft);
        newAircraftMap.delete(airplane.callsign);
        return true;
      } else {
        return false;
      }
    },
  );

  newAircraftMap.forEach((newAircraft) => {
    const newAirplane = new Aircraft(
      newAircraft.callsign,
      newAircraft.position,
      newAircraft.vector,
      newAircraft.type,
      newAircraft.originIata,
      newAircraft.originIcao,
      newAircraft.destinationIata,
      newAircraft.destinationIcao,
      newAircraft.eta,
    );
    radarGame.controlledAirplane.push(newAirplane);
  });
}

// when the start button is clicked, start the game and start fetching aircraft locations
const startButton = document.getElementById("startButton");
startButton?.addEventListener("click", () => {
  radarGame.start();

  console.log("Game started");
  // Start fetching aircraft location every 1 second
  if (!locationUpdateInterval) {
    locationUpdateInterval = setInterval(fetchAircraftLocation, 1000); // 1 second interval
  }
});

// when the stop button is clicked, stop the game and stop fetching aircraft locations
const stopButton = document.getElementById("stopButton");
stopButton?.addEventListener("click", () => {
  radarGame.stop();

  // Stop fetching aircraft location
  if (locationUpdateInterval) {
    clearInterval(locationUpdateInterval);
    locationUpdateInterval = null;
  }
});

const displayRangeElement = document.getElementById("displayRange");
displayRangeElement?.addEventListener("input", (event) => {
  const newRange = parseFloat((event.target as HTMLInputElement).value);
  radarGame.displayRange = newRange;
  radarGame.updateWaypoints();
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

interface CommercialAircraft {
  callsign: string;
  altitude: number;
  location: { positionX: number; positionY: number };
  heading: number;
  speed: number;
  destination: string;
}

// ボタンにクリックイベントリスナーを追加
resetButton?.addEventListener("click", handleResetButtonClick);
