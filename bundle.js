(() => {
  // airplaneClass.ts
  var airlines = ["ANA", "JAL", "APJ", "IBX", "SFJ", "SKY"];
  var destAirport = ["HND", "NRT", "KIX", "ITM", "CTS", "FUK", "NGO", "OKA", "KOJ", "KMJ", "HIJ", "KOI", "FSZ", "NGS", "KCZ", "TAK", "MYJ", "TOY", "AOJ", "AKJ", "AOJ", "AXT", "GAJ", "HKD", "IZO", "IWJ", "MMB", "OKD", "OIT", "ONJ", "RIS", "SDJ", "SYO", "TNE", "TNA", "UBJ", "MMJ", "KKJ", "YGJ", "AOJ", "AXT", "GAJ", "HKD", "IZO", "IWJ", "MMB", "OKD", "OIT", "ONJ", "RIS", "SDJ", "SYO", "TNE", "TNA", "UBJ", "MMJ", "KKJ", "YGJ"];
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
        x: this.location.positionX + 50,
        y: this.location.positionY - 50
      };
    }
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
      return altitude !== void 0 && altitude >= 0;
    }
    getRandomAltitude() {
      return Math.floor(Math.random() * 490 + 10);
    }
    isHeadingValid(heading) {
      return heading !== void 0 && (heading >= 0 && 36 < heading);
    }
    getRandomHeading() {
      return Math.floor(Math.random() * 35);
    }
    isSpeedValid(speed) {
      return speed !== void 0 && (speed >= 100 && 500 < speed);
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
    updateLocation() {
      const speedComponents = this.calculateSpeedComponents(
        this.speed,
        this.heading
      );
      this.location.positionX += speedComponents.x / 1e3;
      this.location.positionY += speedComponents.y / 1e3;
      this.labelLocation.x += speedComponents.x / 1e3;
      this.labelLocation.y += speedComponents.y / 1e3;
    }
    calculateSpeedComponents(speed, heading) {
      if (heading < 0 || heading > 36) {
        throw new Error("Invalid heading. Heading should be between 0 and 36.");
      }
      const radians = heading * 10 * Math.PI / 180;
      const xSpeed = Math.sin(radians) * speed;
      const ySpeed = -Math.cos(radians) * speed;
      return { x: xSpeed, y: ySpeed };
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
      const destination = this.destination;
      const labelX = this.labelLocation.x;
      const labelY = this.labelLocation.y;
      return { callsign, heading, speed, altitude, destination, labelX, labelY };
    }
  };

  // index.ts
  var CANVAS_WIDTH = 1e3;
  var CANVAS_HEIGHT = 1e3;
  var REFRESH_RATE = 30;
  var RadarGame = class {
    canvas;
    //ダブルバッファで画面を切り替えてアニメーションを実現するために配列で定義
    ctx;
    clickedPosition;
    //クリックされた座標を取得
    inGame;
    //シミュレーションゲーム中かどうかを判断する
    bg;
    //ダブルバッファの背景と表示を切り替えるためのインデックスを管理
    controlledAirplane = [];
    constructor() {
      this.canvas = [this.createCanvas("radar"), this.createCanvas("radar2")];
      this.ctx = this.canvas.map(
        (c) => c.getContext("2d")
      );
      this.clickedPosition = null;
      this.inGame = false;
      this.bg = 0;
      this.canvas[0].addEventListener("click", (e) => this.handleClick(e));
      this.canvas[1].addEventListener("click", (e) => this.handleClick(e));
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
      airplane.updateLocation();
    }
    handleClick(event) {
      const canvas = event.target;
      const rect = canvas.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      this.clickedPosition = { x, y };
      for (const airplane of this.controlledAirplane) {
        const position = airplane.currentPosition();
        const aircraftRadius = 50;
        if (x >= position.currentX - aircraftRadius && x <= position.currentX + aircraftRadius && y >= position.currentY - aircraftRadius && y <= position.currentY + aircraftRadius) {
          console.log("Clicked Aircraft Info:");
          console.log(airplane.getAirplaneInfo());
        }
      }
    }
    drawRect(index, airplane) {
      const position = airplane.currentPosition();
      const airplaneInfo = airplane.getAirplaneInfo();
      const labelX = airplaneInfo.labelX;
      const labelY = airplaneInfo.labelY;
      this.ctx[index].beginPath();
      this.ctx[index].rect(position.currentX - 5, position.currentY - 5, 10, 10);
      this.ctx[index].fillStyle = "white";
      this.ctx[index].fill();
      this.ctx[index].beginPath();
      this.ctx[index].moveTo((position.currentX * 8 + labelX * 2) / 10, (position.currentY * 8 + labelY * 2) / 10);
      this.ctx[index].lineTo(labelX - 5, labelY + 15);
      this.ctx[index].strokeStyle = "white";
      this.ctx[index].stroke();
      this.ctx[index].fillStyle = "white";
      this.ctx[index].font = "12px Arial";
      this.ctx[index].textAlign = "left";
      this.ctx[index].fillText(airplaneInfo.callsign, labelX, labelY);
      this.ctx[index].fillText(airplaneInfo.altitude, labelX, labelY + 15);
      this.ctx[index].fillText(airplaneInfo.speed, labelX, labelY + 30);
      this.ctx[index].fillText(airplaneInfo.destination, labelX + 40, labelY + 30);
    }
    toggleCanvasDisplay() {
      this.canvas[1 - this.bg].style.display = "none";
      this.canvas[this.bg].style.display = "block";
      this.bg = 1 - this.bg;
    }
    update() {
      this.clearCanvas(this.bg);
      for (let i = 0; i < this.controlledAirplane.length; i++) {
        this.updatePosition(this.controlledAirplane[i]);
        this.drawRect(this.bg, this.controlledAirplane[i]);
      }
      this.toggleCanvasDisplay();
    }
    start() {
      if (!this.ctx[0] || !this.ctx[1]) {
        console.error("Failed to get 2D context");
        return;
      }
      setInterval(() => {
        this.update();
      }, 1e3 / REFRESH_RATE);
    }
  };
  var radarGame = new RadarGame();
  radarGame.start();
})();
