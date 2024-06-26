// Define constants for canvas dimensions
const CANVAS_WIDTH: number = 1000;
const CANVAS_HEIGHT: number = 1000;
const REFRESH_RATE: number = 10; //画面の更新頻度(fps)

import { Airplane } from "./airplaneClass.ts";
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
  private coordinateManager: CoordinateManager;
  // private sendButton: HTMLInputElement;
  private inGame: boolean; //シミュレーションゲーム中かどうかを判断する
  private bg: number; //ダブルバッファの背景と表示を切り替えるためのインデックスを管理
  private controlledAirplane: Airplane[] = [];
  private displayingWaypoints: Waypoint[] = [];
  private draggingLabelIndex: number = -1; // Index of the label being dragged
  private offsetX: number = 0;
  private offsetY: number = 0;

  private centerCoordinates = { latitude: 38.267371894248924, longitude: 140.86859473251855 };
  public displayRange: number;

  private selectedAircraft: Airplane | null = null; //選択中の航空機を保持するための変数

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
    this.confirmButton = <HTMLInputElement> document.getElementById("confirmButton");
    
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

    //とりあえず10機の航空機を生成する
    for (let i = 1; i <= 10; i++) {
      const airplane = new Airplane();
      this.controlledAirplane.push(airplane);
    }
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

  /**
   * Updates the position of the specified airplane.
   * @param airplane - The airplane to update the position for.
   */
  private updatePosition(airplane: Airplane): void {
    //航空機の速度に合わせてポジションを更新する
    airplane.updateLocation(REFRESH_RATE);
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

    // Store the clicked position
    this.clickedPosition = { x, y };

    const aircraftRadius = 30; // Adjust the radius as needed

    // Iterate through the aircraft to check if they are near the clicked position
    for (let i = 0; i < this.controlledAirplane.length; i++) {//(const airplane of this.controlledAirplane) {
      const position = this.controlledAirplane[i].currentPosition();
      const airplaneInfo = this.controlledAirplane[i].getAirplaneInfo();
      const labelX: number = this.controlledAirplane[i].currentPosition().currentX +
        airplaneInfo.labelLocation.x;
      const labelY: number = this.controlledAirplane[i].currentPosition().currentY -
        airplaneInfo.labelLocation.y;

      // Check if the clicked position is near the aircraft's position
      if (
        x >= position.currentX - aircraftRadius &&
        x <= position.currentX + aircraftRadius &&
        y >= position.currentY - aircraftRadius &&
        y <= position.currentY + aircraftRadius
      ) {
        // Log the aircraft information to the console
        this.changeDisplayCallsign(airplaneInfo.callsign);
        this.inputAltitude.value = airplaneInfo.commandedAltitude;
        this.inputSpeed.value = airplaneInfo.commandedSpeed;
        this.inputHeading.value = airplaneInfo.commandedHeading;
        //選ばれている航空機を保持する
        this.selectedAircraft = this.controlledAirplane[i];
        // console.log("clicked Airplane!");
        break;

      } else if (
        x >= labelX - 5 &&
        x <= labelX + 70 &&
        y >= labelY - 20 &&
        y <= labelY + 40
        
      ) {
        // console.log("Label clicked!");
        // console.log(airplaneInfo.callsign);
        this.draggingLabelIndex = i;
        this.offsetX = x - labelX;
        this.offsetY = y - labelY;
        break; // Exit loop once a label is found
      }
    }
  }

  /**
   * Handles the mouse move event.
   * @param event - The mouse move event.
   */
  private onMouseMove(event: MouseEvent): void {
    if (this.draggingLabelIndex !== -1) {
      const canvas = event.target as HTMLCanvasElement;
      const rect = canvas.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
  
      // Update the label position based on the mouse movement
      const label = this.controlledAirplane[this.draggingLabelIndex].getAirplaneInfo().labelLocation;
      label.x = x - this.offsetX - this.controlledAirplane[this.draggingLabelIndex].currentPosition().currentX;
      label.y = - (y - this.offsetY - this.controlledAirplane[this.draggingLabelIndex].currentPosition().currentY);
    }
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
  private drawAirplane(index: number, airplane: Airplane): void {
    const position = airplane.currentPosition();
    const radius: number = 5;

    // Draw airplane as a filled white circle
    this.ctx[index].beginPath();
    this.ctx[index].arc(
      position.currentX,
      position.currentY,
      radius,
      0,
      2 * Math.PI,
    );
    this.ctx[index].fillStyle = "white";
    this.ctx[index].fill();
  }

  /**
   * Draws the heading line representing airplane movement direction.
   * @param index - The index of the airplane in the list.
   * @param airplane - The instance of the Airplane class.
   */
  private drawHeadingLine(index: number, airplane: Airplane): void {
    const position = airplane.currentPosition();
    const airplaneInfo = airplane.getAirplaneInfo();
    const speed: number = Number(airplaneInfo.speed);
    const heading: number = Number(airplaneInfo.heading);

    // Draw a line indicating the direction of airplane movement
    this.ctx[index].beginPath();
    this.ctx[index].moveTo(position.currentX, position.currentY);

    const currentSpeed = airplane.calculateSpeedComponents(speed, heading);
    this.ctx[index].lineTo(
      position.currentX + (currentSpeed.xSpeed * 60),// * REFRESH_RATE * 60),
      position.currentY + (currentSpeed.ySpeed * 60)// * REFRESH_RATE * 60),
    );

    this.ctx[index].strokeStyle = "white";
    this.ctx[index].stroke();
  }

  /**
   * Draws the line connecting airplane to its label position.
   * @param index - The index of the airplane in the list.
   * @param airplane - The instance of the Airplane class.
   */
  private drawLabelLine(index: number, airplane: Airplane): void {
    const position = airplane.currentPosition();
    const airplaneInfo = airplane.getAirplaneInfo();
    const labelX: number = position.currentX + airplaneInfo.labelLocation.x;
    const labelY: number = position.currentY - airplaneInfo.labelLocation.y;
    const labelDistance = Math.sqrt(Math.pow(airplaneInfo.labelLocation.x, 2) + Math.pow(airplaneInfo.labelLocation.y, 2));
    const sin = airplaneInfo.labelLocation.y / labelDistance;
    const cos = airplaneInfo.labelLocation.x / labelDistance;

    // Draw a line connecting airplane to its label
    this.ctx[index].beginPath();
    this.ctx[index].moveTo(position.currentX + (10 * cos), position.currentY - (10 * sin));
    this.ctx[index].lineTo(labelX - 5, labelY + 15);
    this.ctx[index].strokeStyle = "white";
    this.ctx[index].stroke();

    //ラベルの周りを線で囲いたいとき↓↓
    // this.ctx[index].beginPath();
    // this.ctx[index].strokeStyle = "white";
    // this.ctx[index].lineWidth = 2;
    // this.ctx[index].moveTo(labelX-5, labelY-20); // Move to the starting point
    // this.ctx[index].lineTo(labelX + 70, labelY-20); // Draw top line
    // this.ctx[index].lineTo(labelX + 70, labelY + 40); // Draw right line
    // this.ctx[index].lineTo(labelX-5, labelY + 40); // Draw bottom line
    // this.ctx[index].lineTo(labelX-5, labelY-20); // Draw left line to close the rectangle
    // this.ctx[index].stroke(); // Stroke the path
  }

  /**
   * Draw the label containing airplane information.
   * @param index - Index of the airplane in the list.
   * @param airplane - Instance of the Airplane class.
   */
  private drawLabel(index: number, airplane: Airplane): void {
    const airplaneInfo = airplane.getAirplaneInfo();
    const airplanePosition = airplane.currentPosition();
    const labelX: number = airplanePosition.currentX + airplaneInfo.labelLocation.x;
    const labelY: number = airplanePosition.currentY - airplaneInfo.labelLocation.y;

    // Draw labels with airplane information
    this.ctx[index].fillStyle = "white";
    this.ctx[index].font = "12px Arial";
    this.ctx[index].textAlign = "left";

    this.ctx[index].fillText(airplaneInfo.callsign, labelX, labelY);
    this.ctx[index].fillText(airplaneInfo.altitude, labelX, labelY + 15);
    this.ctx[index].fillText(airplaneInfo.speed, labelX, labelY + 30);
    this.ctx[index].fillText(
      airplaneInfo.destination,
      labelX + 40,
      labelY + 30,
    );
  }

  /**
   * Draw the airplane details on the canvas.
   * @param index - Index of the airplane in the list.
   * @param airplane - Instance of the Airplane class.
   */
  public drawAirplaneDetails(index: number, airplane: Airplane): void {
    // Draw airplane representation, heading line, label line, and label
    this.drawAirplane(index, airplane);
    this.drawHeadingLine(index, airplane);
    this.drawLabelLine(index, airplane);
    this.drawLabel(index, airplane);
  }

  public drawWaypoint(index: number, name: string, latitude: number, longitude: number): void {
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

  private send_command(): void {
    if (this.selectedAircraft) {
      const { newAltitude, newSpeed, newHeading } = this.selectedAircraft
        .changeCommandedInfo(
          Number(this.inputAltitude.value),
          Number(this.inputSpeed.value),
          Number(this.inputHeading.value),
        );

      // Now you have the extracted values in newAltitude, newSpeed, and newHeading
      this.inputAltitude.value = newAltitude;
      this.inputSpeed.value = newSpeed;
      this.inputHeading.value = newHeading;
    }
    console.log(this.selectedAircraft);
  }

  private update(): void {
    //画面全体を更新する
    this.clearCanvas(this.bg);
    for (let i = 0; i < this.displayingWaypoints.length; i++) {
      this.drawWaypoint(this.bg, this.displayingWaypoints[i].name, this.displayingWaypoints[i].latitude, this.displayingWaypoints[i].longitude);
    }
    for (let i = 0; i < this.controlledAirplane.length; i++) {
      this.updatePosition(this.controlledAirplane[i]);
      this.drawAirplaneDetails(this.bg, this.controlledAirplane[i]);
    }
    this.toggleCanvasDisplay();
  }

  public updateWaypoints(): void {
    // Update the waypoints based on the center coordinates and display range
    this.waypointManager.updateFilteredWaypoints(this.centerCoordinates, this.displayRange);
    this.displayingWaypoints = this.waypointManager.getFilteredWaypoints();
    // Calculate the canvas coordinates for the waypoints
    for (let i = 0; i < this.displayingWaypoints.length; i++) {
      const { x, y } = this.coordinateManager.calculateCanvasCoordinates(
        this.centerCoordinates.latitude, 
        this.centerCoordinates.longitude, 
        this.displayRange, 
        this.displayingWaypoints[i].latitude, 
        this.displayingWaypoints[i].longitude
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
      console.log('Game stopped');
    }
  }
}

// Initialize and start the game
const radarGame = new RadarGame();

// when the page is loaded, make the first canvas
radarGame.makeFirstCanvas();

// when the start button is clicked, start the game
const startButton = document.getElementById('startButton');
startButton?.addEventListener('click', () => {
  radarGame.start();
});

// when the stop button is clicked, stop the game
const stopButton = document.getElementById('stopButton');
if (stopButton) {
  stopButton.addEventListener('click', () => {
    radarGame.stop();
  });
}

const displayRangeElement = document.getElementById('displayRange');
if (displayRangeElement) {
  displayRangeElement.addEventListener('input', (event) => {
    const newRange = parseFloat((event.target as HTMLInputElement).value);
    radarGame.displayRange = newRange;
    radarGame.updateWaypoints();
  });
}
