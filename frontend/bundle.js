(() => {
  // frontend/scripts/airplaneClass.ts
  var airlines = ["ANA", "JAL", "APJ", "IBX", "SFJ", "SKY", "FDA", "ADO", "SNJ", "AHX"];
  var destAirport = [
    "HND",
    "NRT",
    "KIX",
    "ITM",
    "CTS",
    "FUK",
    "NGO",
    "OKA",
    "KOJ",
    "KMJ",
    "HIJ",
    "KOI",
    "FSZ",
    "NGS",
    "KCZ",
    "TAK",
    "MYJ",
    "TOY",
    "AOJ",
    "AKJ",
    "AOJ",
    "AXT",
    "GAJ",
    "HKD",
    "IZO",
    "IWJ",
    "MMB",
    "OKD",
    "OIT",
    "ONJ",
    "RIS",
    "SDJ",
    "SYO",
    "TNE",
    "TNA",
    "UBJ",
    "MMJ",
    "KKJ",
    "YGJ",
    "AOJ",
    "AXT",
    "GAJ",
    "HKD",
    "IZO",
    "IWJ",
    "MMB",
    "OKD",
    "OIT",
    "ONJ",
    "RIS",
    "SDJ",
    "SYO",
    "TNE",
    "TNA",
    "UBJ",
    "MMJ",
    "KKJ",
    "YGJ"
  ];
  var updateRange = 50;
  var Airplane = class {
    callsign;
    altitude;
    commandedAltitude;
    location;
    heading;
    commandedHeading;
    speed;
    commandedSpeed;
    destination;
    labelLocation;
    draggingLabelIndex;
    constructor(callsign, altitude, location, heading, speed, destination) {
      this.callsign = this.isCallsignValid(callsign) ? callsign : this.getRandomCallsing();
      this.altitude = this.isAltitudeValid(altitude) ? altitude : this.getRandomAltitude();
      this.commandedAltitude = this.altitude;
      this.location = location || this.getRandomLocation();
      this.heading = this.isHeadingValid(heading) ? heading : this.getRandomHeading();
      this.commandedHeading = this.heading;
      this.speed = this.isSpeedValid(speed) ? speed : this.getRandomSpeed();
      this.commandedSpeed = this.speed;
      this.destination = this.isCallsignValid(destination) ? destination : this.getRandomDestination();
      this.labelLocation = {
        //デフォルトでは右上に表示
        x: 50,
        y: 50
      };
      this.draggingLabelIndex = -1;
    }
    /**
     * コールサインとして適切なものが入力されているかどうかを判定する関数
     * @param callsign 
     * @returns {boolean} callsingが入力されており、かつ""でない時にtrueを返す
     */
    isCallsignValid(callsign) {
      return callsign !== void 0 && callsign !== "";
    }
    getRandomCallsing() {
      let randomCallsign;
      const randomIndex = Math.floor(Math.random() * airlines.length);
      const randomCallNumber = String(
        Math.floor(Math.random() * 2e3 + 100)
      );
      randomCallsign = airlines[randomIndex] + randomCallNumber;
      return randomCallsign;
    }
    isAltitudeValid(altitude) {
      return altitude !== void 0 && (altitude >= 0 && altitude < 500);
    }
    getRandomAltitude() {
      return Math.floor(Math.random() * 490 + 10);
    }
    isHeadingValid(heading) {
      return heading !== void 0 && (heading >= 0 && heading < 36);
    }
    getRandomHeading() {
      return Math.floor(Math.random() * 35);
    }
    isSpeedValid(speed) {
      return speed !== void 0 && (speed >= 100 && speed < 500);
    }
    getRandomSpeed() {
      return Math.floor(Math.random() * 400 + 100);
    }
    getRandomLocation() {
      return {
        positionX: Math.random() * 1e3,
        positionY: Math.random() * 1e3
      };
    }
    getRandomDestination() {
      let randomDestination;
      const randomIndex = Math.floor(Math.random() * destAirport.length);
      randomDestination = destAirport[randomIndex];
      return randomDestination;
    }
    // 航空機の表示を操作するための関数↓↓↓↓↓↓↓↓↓↓
    updateLocation(refleshRate) {
      const speedComponents = this.calculateSpeedComponents(this.speed, this.heading);
      this.location.positionX += speedComponents.xSpeed / refleshRate;
      this.location.positionY += speedComponents.ySpeed / refleshRate;
    }
    /**
     * スピードとヘディングから現在のx方向のスピードとy方向のスピードを計算して返します。
      * @param {number} speed - 速度
      * @param {number} heading - 方位（0から36の範囲）
      * @returns {SpeedVector} - 速度のX成分とY成分を含むオブジェクト
     */
    calculateSpeedComponents(speed, heading) {
      if (heading < 0 || heading > 36) {
        throw new Error("Invalid heading. Heading should be between 0 and 36.");
      }
      const radians = heading * 10 * Math.PI / 180;
      const xSpeed = Math.sin(radians) * speed / updateRange;
      const ySpeed = -Math.cos(radians) * speed / updateRange;
      return new SpeedVector(xSpeed, ySpeed);
    }
    currentPosition() {
      const currentX = this.location.positionX;
      const currentY = this.location.positionY;
      return { currentX, currentY };
    }
    getAirplaneInfo() {
      const callsign = this.callsign;
      const heading = String(this.heading);
      const speed = String(this.speed);
      const altitude = String(this.altitude);
      const commandedHeading = String(this.commandedHeading);
      const commandedSpeed = String(this.commandedSpeed);
      const commandedAltitude = String(this.commandedAltitude);
      const destination = this.destination;
      const labelLocation = this.labelLocation;
      return {
        callsign,
        heading,
        speed,
        altitude,
        commandedHeading,
        commandedSpeed,
        commandedAltitude,
        destination,
        labelLocation
      };
    }
    changeCommandedInfo(inputAltitude, inputSpeed, inputHeading) {
      const newAltitude = this.isAltitudeValid(inputAltitude) ? inputAltitude : this.altitude;
      this.commandedAltitude = newAltitude;
      const newSpeed = this.isSpeedValid(inputSpeed) ? inputSpeed : this.speed;
      this.commandedSpeed = newSpeed;
      const newHeading = this.isHeadingValid(inputHeading) ? inputHeading : this.heading;
      this.commandedHeading = newHeading;
      this.altitude = this.commandedAltitude;
      this.speed = this.commandedSpeed;
      this.heading = this.commandedHeading;
      return {
        newAltitude: String(newAltitude),
        newSpeed: String(newSpeed),
        newHeading: String(newHeading)
      };
    }
  };
  var SpeedVector = class {
    constructor(xSpeed, ySpeed) {
      this.xSpeed = xSpeed;
      this.ySpeed = ySpeed;
    }
  };

  // frontend/scripts/index.ts
  var CANVAS_WIDTH = 1e3;
  var CANVAS_HEIGHT = 1e3;
  var REFRESH_RATE = 10;
  var RadarGame = class {
    canvas;
    //ダブルバッファで画面を切り替えてアニメーションを実現するために配列で定義
    ctx;
    clickedPosition;
    //クリックされた座標を取得
    displayCallsign;
    inputAltitude;
    inputSpeed;
    inputHeading;
    confirmButton;
    // private sendButton: HTMLInputElement;
    inGame;
    //シミュレーションゲーム中かどうかを判断する
    bg;
    //ダブルバッファの背景と表示を切り替えるためのインデックスを管理
    controlledAirplane = [];
    draggingLabelIndex = -1;
    // Index of the label being dragged
    offsetX = 0;
    offsetY = 0;
    selectedAircraft = null;
    //選択中の航空機を保持するための変数
    constructor() {
      this.canvas = [this.createCanvas("radar"), this.createCanvas("radar2")];
      this.ctx = this.canvas.map(
        (c) => c.getContext("2d")
      );
      this.clickedPosition = null;
      this.displayCallsign = document.getElementById("callsign");
      this.inputAltitude = document.getElementById("altitude");
      this.inputSpeed = document.getElementById("speed");
      this.inputHeading = document.getElementById("heading");
      this.confirmButton = document.getElementById(
        "confirmButton"
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
      for (let i = 1; i <= 10; i++) {
        const airplane = new Airplane();
        this.controlledAirplane.push(airplane);
      }
    }
    createCanvas(id) {
      const canvas = document.getElementById(id);
      if (!canvas) {
        throw new Error(`Canvas element with id "${id}" not found.`);
      }
      return canvas;
    }
    clearCanvas(index) {
      this.ctx[index].fillStyle = "black";
      this.ctx[index].fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    updatePosition(airplane) {
      airplane.updateLocation(REFRESH_RATE);
    }
    /**
     * Gets a mouse click event and returns the aircraft closest to the clicked location.
     * @param MouseEvent - get mouse click event
     * @return - None
     */
    onMouseDown(event) {
      const canvas = event.target;
      const rect = canvas.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      this.clickedPosition = { x, y };
      const aircraftRadius = 30;
      for (let i = 0; i < this.controlledAirplane.length; i++) {
        const position = this.controlledAirplane[i].currentPosition();
        const airplaneInfo = this.controlledAirplane[i].getAirplaneInfo();
        const labelX = this.controlledAirplane[i].currentPosition().currentX + airplaneInfo.labelLocation.x;
        const labelY = this.controlledAirplane[i].currentPosition().currentY - airplaneInfo.labelLocation.y;
        if (x >= position.currentX - aircraftRadius && x <= position.currentX + aircraftRadius && y >= position.currentY - aircraftRadius && y <= position.currentY + aircraftRadius) {
          this.changeDisplayCallsign(airplaneInfo.callsign);
          this.inputAltitude.value = airplaneInfo.commandedAltitude;
          this.inputSpeed.value = airplaneInfo.commandedSpeed;
          this.inputHeading.value = airplaneInfo.commandedHeading;
          this.selectedAircraft = this.controlledAirplane[i];
          console.log("clicked Airplane!");
          break;
        } else if (x >= labelX - 5 && x <= labelX + 70 && y >= labelY - 20 && y <= labelY + 40) {
          this.draggingLabelIndex = i;
          this.offsetX = x - labelX;
          this.offsetY = y - labelY;
          break;
        } else {
          console.log("Now Clicked!!");
        }
      }
    }
    onMouseMove(event) {
      if (this.draggingLabelIndex !== -1) {
        const canvas = event.target;
        const rect = canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        const label = this.controlledAirplane[this.draggingLabelIndex].getAirplaneInfo().labelLocation;
        label.x = x - this.offsetX - this.controlledAirplane[this.draggingLabelIndex].currentPosition().currentX;
        label.y = -(y - this.offsetY - this.controlledAirplane[this.draggingLabelIndex].currentPosition().currentY);
      }
    }
    onMouseUp() {
      this.draggingLabelIndex = -1;
    }
    changeDisplayCallsign(newCallsign) {
      const fontElement = document.getElementById(
        "callsign"
      );
      if (fontElement) {
        fontElement.textContent = newCallsign;
      }
    }
    /**
     * Draw the airplane representation on the canvas.
     * @param index - Index of the airplane in the list.
     * @param airplane - Instance of the Airplane class.
     */
    drawAirplane(index, airplane) {
      const position = airplane.currentPosition();
      const radius = 5;
      this.ctx[index].beginPath();
      this.ctx[index].arc(
        position.currentX,
        position.currentY,
        radius,
        0,
        2 * Math.PI
      );
      this.ctx[index].fillStyle = "white";
      this.ctx[index].fill();
    }
    /**
     * Draw the heading line representing airplane movement direction.
     * @param index - Index of the airplane in the list.
     * @param airplane - Instance of the Airplane class.
     */
    drawHeadingLine(index, airplane) {
      const position = airplane.currentPosition();
      const airplaneInfo = airplane.getAirplaneInfo();
      const speed = Number(airplaneInfo.speed);
      const heading = Number(airplaneInfo.heading);
      this.ctx[index].beginPath();
      this.ctx[index].moveTo(position.currentX, position.currentY);
      const currentSpeed = airplane.calculateSpeedComponents(speed, heading);
      this.ctx[index].lineTo(
        position.currentX + currentSpeed.xSpeed * 60,
        // * REFRESH_RATE * 60),
        position.currentY + currentSpeed.ySpeed * 60
        // * REFRESH_RATE * 60),
      );
      this.ctx[index].strokeStyle = "white";
      this.ctx[index].stroke();
    }
    /**
     * Draw the line connecting airplane to its label position.
     * @param index - Index of the airplane in the list.
     * @param airplane - Instance of the Airplane class.
     */
    drawLabelLine(index, airplane) {
      const position = airplane.currentPosition();
      const airplaneInfo = airplane.getAirplaneInfo();
      const labelX = airplane.currentPosition().currentX + airplaneInfo.labelLocation.x;
      const labelY = airplane.currentPosition().currentY - airplaneInfo.labelLocation.y;
      this.ctx[index].beginPath();
      this.ctx[index].moveTo(position.currentX + 10, position.currentY - 10);
      this.ctx[index].lineTo(labelX - 5, labelY + 15);
      this.ctx[index].strokeStyle = "white";
      this.ctx[index].stroke();
      this.ctx[index].beginPath();
      this.ctx[index].strokeStyle = "white";
      this.ctx[index].lineWidth = 2;
      this.ctx[index].moveTo(labelX - 5, labelY - 20);
      this.ctx[index].lineTo(labelX + 70, labelY - 20);
      this.ctx[index].lineTo(labelX + 70, labelY + 40);
      this.ctx[index].lineTo(labelX - 5, labelY + 40);
      this.ctx[index].lineTo(labelX - 5, labelY - 20);
      this.ctx[index].stroke();
    }
    /**
     * Draw the label containing airplane information.
     * @param index - Index of the airplane in the list.
     * @param airplane - Instance of the Airplane class.
     */
    drawLabel(index, airplane) {
      const airplaneInfo = airplane.getAirplaneInfo();
      const airplanePosition = airplane.currentPosition();
      const labelX = airplanePosition.currentX + airplaneInfo.labelLocation.x;
      const labelY = airplanePosition.currentY - airplaneInfo.labelLocation.y;
      this.ctx[index].fillStyle = "white";
      this.ctx[index].font = "12px Arial";
      this.ctx[index].textAlign = "left";
      this.ctx[index].fillText(airplaneInfo.callsign, labelX, labelY);
      this.ctx[index].fillText(airplaneInfo.altitude, labelX, labelY + 15);
      this.ctx[index].fillText(airplaneInfo.speed, labelX, labelY + 30);
      this.ctx[index].fillText(
        airplaneInfo.destination,
        labelX + 40,
        labelY + 30
      );
    }
    /**
     * Draw the airplane details on the canvas.
     * @param index - Index of the airplane in the list.
     * @param airplane - Instance of the Airplane class.
     */
    drawAirplaneDetails(index, airplane) {
      this.drawAirplane(index, airplane);
      this.drawHeadingLine(index, airplane);
      this.drawLabelLine(index, airplane);
      this.drawLabel(index, airplane);
    }
    toggleCanvasDisplay() {
      this.canvas[1 - this.bg].style.display = "none";
      this.canvas[this.bg].style.display = "block";
      this.bg = 1 - this.bg;
    }
    send_command() {
      if (this.selectedAircraft) {
        const { newAltitude, newSpeed, newHeading } = this.selectedAircraft.changeCommandedInfo(
          Number(this.inputAltitude.value),
          Number(this.inputSpeed.value),
          Number(this.inputHeading.value)
        );
        this.inputAltitude.value = newAltitude;
        this.inputSpeed.value = newSpeed;
        this.inputHeading.value = newHeading;
      }
      console.log(this.selectedAircraft);
    }
    update() {
      this.clearCanvas(this.bg);
      for (let i = 0; i < this.controlledAirplane.length; i++) {
        this.updatePosition(this.controlledAirplane[i]);
        this.drawAirplaneDetails(this.bg, this.controlledAirplane[i]);
      }
      this.toggleCanvasDisplay();
    }
    start() {
      if (!this.ctx[0] || !this.ctx[1]) {
        console.error("Failed to get 2D context");
        return;
      }
      this.update();
      setInterval(() => {
        this.update();
      }, 1e3 / REFRESH_RATE);
    }
  };
  var radarGame = new RadarGame();
  radarGame.start();
})();
