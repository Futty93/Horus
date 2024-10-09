(() => {
  // Frontend/scripts/AtsRouteManager/atsRoutesLoader.ts
  async function loadAtsRoutes() {
    try {
      const response = await fetch("http://localhost:8080/ats/route/all");
      const data = await response.json();
      const waypoints = data.waypoints.map((wp) => ({
        name: wp.name,
        latitude: parseFloat(wp.latitude),
        longitude: parseFloat(wp.longitude),
        type: wp.type
      }));
      const radioNavigationAids = data.radioNavigationAids.map((aid) => ({
        name: aid.name,
        id: aid.id,
        type: aid.type,
        latitude: aid.latitude,
        longitude: aid.longitude,
        frequency: aid.frequency
      }));
      const atsLowerRoutes = data.atsLowerRoutes.map((route) => ({
        name: route.name,
        description: route.description,
        points: route.points.map((point) => ({
          name: point.name,
          latitude: point.latitude,
          longitude: point.longitude,
          type: point.type
        }))
      }));
      const rnavRoutes = data.rnavRoutes.map((route) => ({
        name: route.name,
        description: route.description,
        points: route.points.map((point) => ({
          name: point.name,
          latitude: point.latitude,
          longitude: point.longitude,
          type: point.type
        }))
      }));
      return { waypoints, radioNavigationAids, atsLowerRoutes, rnavRoutes };
    } catch (error) {
      console.error("Failed to load ATS routes:", error);
      throw error;
    }
  }
  var atsRoutesLoader_default = loadAtsRoutes;

  // Frontend/scripts/globals/constants.ts
  var GLOBAL_CONSTANTS = {
    EARTH_RADIUS_KM: 6378.1,
    CANVAS_BACKGROUND_COLOR: "#FFFFFF",
    WAYPOINT_COLOR: "#00f",
    NAV_AID_COLOR: "#f00",
    LINE_COLOR: "#000",
    FONT_STYLE_IN_CANVAS: "10px Arial",
    REFRESH_RATE: 60
  };

  // Frontend/scripts/globals/settings.ts
  var GLOBAL_SETTINGS = {
    canvasWidth: 2e3,
    canvasHeight: 2e3,
    displayRange: 200,
    centerCoordinates: { latitude: 35.6895, longitude: 139.6917 },
    // 東京の座標
    isDisplaying: {
      waypointName: true,
      waypointPoint: true,
      radioNavigationAidsName: true,
      radioNavigationAidsPoint: true,
      atsLowerRoute: true,
      rnavRoute: true
    },
    locationUpdateInterval: null
    // 更新間隔（ms）
  };

  // Frontend/scripts/coordinateManager/CoordinateManager.ts
  var CoordinateManager = class {
    /**
     * 緯度・経度をキャンバス上の座標に変換します。
     * @param centerLatitude 中心の緯度
     * @param centerLongitude 中心の経度
     * @param range 表示領域の1辺の長さ（キロメートル）
     * @param targetLatitude 対象の緯度
     * @param targetLongitude 対象の経度
     * @returns キャンバス上の座標 {x, y}
     */
    static calculateCanvasCoordinates(pointLat, pointLon) {
      const toRadians = (degrees) => degrees * (Math.PI / 180);
      const deltaLat = toRadians(pointLat - GLOBAL_SETTINGS.centerCoordinates.latitude);
      const deltaLon = toRadians(pointLon - GLOBAL_SETTINGS.centerCoordinates.longitude);
      const a = Math.sin(deltaLat / 2) ** 2 + Math.cos(toRadians(GLOBAL_SETTINGS.centerCoordinates.latitude)) * Math.cos(toRadians(pointLat)) * Math.sin(deltaLon / 2) ** 2;
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      const distanceKm = GLOBAL_CONSTANTS.EARTH_RADIUS_KM * c;
      const y = Math.sin(deltaLon) * Math.cos(toRadians(pointLat));
      const x = Math.cos(toRadians(GLOBAL_SETTINGS.centerCoordinates.latitude)) * Math.sin(toRadians(pointLat)) - Math.sin(toRadians(GLOBAL_SETTINGS.centerCoordinates.latitude)) * Math.cos(toRadians(pointLat)) * Math.cos(deltaLon);
      const bearing = Math.atan2(y, x);
      const pixelsPerKm = GLOBAL_SETTINGS.canvasWidth / GLOBAL_SETTINGS.displayRange;
      const distancePx = distanceKm * pixelsPerKm;
      const canvasX = GLOBAL_SETTINGS.canvasWidth / 2 + distancePx * Math.sin(bearing);
      const canvasY = GLOBAL_SETTINGS.canvasHeight / 2 - distancePx * Math.cos(bearing);
      return { x: canvasX, y: canvasY };
    }
    /**
     * 度をラジアンに変換します。
     * @param deg 度
     * @returns ラジアン
     */
    degToRad(deg) {
      return deg * (Math.PI / 180);
    }
    /**
     * 2つの座標間の距離を計算します。
     * @param lat1 座標1の緯度
     * @param lon1 座標1の経度
     * @param lat2 座標2の緯度
     * @param lon2 座標2の経度
     * @returns 距離（キロメートル）
     */
    calculateDistance(lat1, lon1, lat2, lon2) {
      const dLat = this.degToRad(lat2 - lat1);
      const dLon = this.degToRad(lon2 - lon1);
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(this.degToRad(lat1)) * Math.cos(this.degToRad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      const distance = GLOBAL_CONSTANTS.EARTH_RADIUS_KM * c;
      return distance;
    }
    /**
     * Calculates the canvas position of an aircraft 1 minute into the future based on its current speed and heading.
     * @param speed - Current speed of the aircraft in knots.
     * @param heading - Current heading of the aircraft in degrees.
     * @param canvasWidth - Width of the canvas in pixels.
     * @param canvasHeight - Height of the canvas in pixels.
     * @param displayRange - The width of the displayed airspace in kilometers.
     * @param currentPosition - Current position of the aircraft on the canvas in pixels.
     * @returns The position of the aircraft on the canvas after 1 minute.
     */
    static calculateFuturePositionOnCanvas(speed, heading, canvasWidth, canvasHeight, displayRange, currentPosition) {
      const speedKmPerMin = speed * 1.852 / 60;
      const headingRad = (heading - 90) * (Math.PI / 180);
      const deltaX = speedKmPerMin * Math.cos(headingRad);
      const deltaY = -speedKmPerMin * Math.sin(headingRad);
      const futurePositionX = currentPosition.x + deltaX;
      const futurePositionY = currentPosition.y + deltaY;
      const kmPerPixel = displayRange / canvasWidth;
      const futureX = currentPosition.x + (futurePositionX - currentPosition.x) / kmPerPixel;
      const futureY = currentPosition.y - (futurePositionY - currentPosition.y) / kmPerPixel;
      return {
        futureX: Math.min(Math.max(futureX, 0), canvasWidth),
        futureY: Math.min(Math.max(futureY, 0), canvasHeight)
      };
    }
  };
  var CoordinateManager_default = CoordinateManager;

  // Frontend/scripts/AtsRouteManager/routeRenderer.ts
  function renderMap(waypoints, radioNavAids, atsRoutes, rnavRoutes, ctx) {
    if (GLOBAL_SETTINGS.isDisplaying.atsLowerRoute) {
      [...atsRoutes].forEach((route) => {
        drawRoute(ctx, route, "#0ff");
      });
    }
    if (GLOBAL_SETTINGS.isDisplaying.rnavRoute) {
      [...rnavRoutes].forEach((route) => {
        drawRoute(ctx, route, "#ff0");
      });
    }
    [...waypoints].forEach((point) => {
      drawPoint(ctx, point, "#0f0", GLOBAL_SETTINGS.isDisplaying.waypointName, GLOBAL_SETTINGS.isDisplaying.waypointPoint);
    });
    [...radioNavAids].forEach((point) => {
      drawPoint(ctx, point, "#f8b", GLOBAL_SETTINGS.isDisplaying.radioNavigationAidsName, GLOBAL_SETTINGS.isDisplaying.radioNavigationAidsPoint);
    });
  }
  function drawPoint(ctx, point, color, isDisplayingName, isDisplayingPoint) {
    const { x, y } = CoordinateManager.calculateCanvasCoordinates(point.latitude, point.longitude);
    const markerSize = 5;
    if (isDisplayingPoint) {
      ctx.beginPath();
      if (point["type"] !== "waypoint") {
        ctx.arc(x, y, markerSize, 0, 2 * Math.PI);
        ctx.strokeStyle = color;
      } else {
        ctx.moveTo(x - markerSize, y);
        ctx.lineTo(x + markerSize, y);
        ctx.moveTo(x, y - markerSize);
        ctx.lineTo(x, y + markerSize);
        ctx.strokeStyle = color;
      }
      ctx.stroke();
    }
    if (isDisplayingName) {
      ctx.font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
      ctx.fillStyle = color;
      ctx.fillText(point.name, x + 7, y - 7);
    }
  }
  function drawRoute(ctx, route, color) {
    const points = route.points;
    for (let i = 0; i < points.length - 1; i++) {
      drawLineBetweenPoints(ctx, points[i], points[i + 1], color);
    }
  }
  function drawLineBetweenPoints(ctx, point1, point2, color) {
    const point1Coordinate = CoordinateManager.calculateCanvasCoordinates(point1.latitude, point1.longitude);
    const point2Coordinate = CoordinateManager.calculateCanvasCoordinates(point2.latitude, point2.longitude);
    ctx.beginPath();
    ctx.moveTo(point1Coordinate.x, point1Coordinate.y);
    ctx.lineTo(point2Coordinate.x, point2Coordinate.y);
    ctx.strokeStyle = color;
    ctx.stroke();
  }

  // Frontend/scripts/aircraft/aircraftClass.ts
  var Aircraft = class {
    callsign;
    position;
    vector;
    instructedVector;
    type;
    originIata;
    originIcao;
    destinationIata;
    destinationIcao;
    eta;
    // You may want to change this to a Date object if needed
    label;
    constructor(callsign, position, vector, instructedVector, type, originIata, originIcao, destinationIata, destinationIcao, eta, labelX = 50, labelY = 50) {
      this.callsign = callsign;
      this.position = position;
      this.vector = vector;
      this.instructedVector = instructedVector;
      this.type = type;
      this.originIata = originIata;
      this.originIcao = originIcao;
      this.destinationIata = destinationIata;
      this.destinationIcao = destinationIcao;
      this.eta = eta;
      this.label = { x: labelX, y: labelY };
    }
    updateAircraftInfo(newAircraft) {
      this.position = newAircraft.position;
      this.vector = newAircraft.vector;
      this.instructedVector = newAircraft.instructedVector;
      this.eta = newAircraft.eta;
    }
  };

  // Frontend/scripts/api/location.ts
  var fetchAircraftLocation = async (controllingAircrafts) => {
    let updatedControllingAircraft = [];
    try {
      const response = await fetch(
        "http://localhost:8080/aircraft/location/all",
        {
          method: "GET",
          headers: {
            "accept": "*/*"
            // Assuming the server sends a custom format
          }
        }
      );
      if (response.ok) {
        const textData = await response.text();
        const aircraftData = parseAircraftData(textData);
        if (aircraftData) {
          updatedControllingAircraft = updateControllingAircrafts(aircraftData, controllingAircrafts);
        } else {
          console.error("Failed to parse aircraft data");
        }
      } else {
        console.error("Request failed with status:", response.status);
      }
    } catch (error) {
      console.error("Error occurred while fetching aircraft location:", error);
    }
    return updatedControllingAircraft;
  };
  var parseAircraftData = (data) => {
    try {
      const aircraftStrings = data.split("\n").filter(
        (line) => line.startsWith("CommercialAircraft")
      );
      return aircraftStrings.map((aircraftString) => {
        return parseAircraftString(aircraftString);
      });
    } catch (error) {
      console.error("Error parsing aircraft data:", error);
      return null;
    }
  };
  var parseAircraftString = (aircraftString) => {
    const aircraftRegex = /callsign=(.*?), position=\{latitude=(.*?), longitude=(.*?), altitude=(.*?)\}, vector=\{heading=(.*?), groundSpeed=(.*?), verticalSpeed=(.*?)\}, instructedVector=\{heading=(.*?), groundSpeed=(.*?), altitude=(.*?)\}, type=(.*?), originIata=(.*?), originIcao=(.*?), destinationIata=(.*?), destinationIcao=(.*?), eta=(.*?)\}/;
    const matches = aircraftString.match(aircraftRegex);
    if (matches) {
      const [
        _,
        callsign,
        lat,
        lon,
        altitude,
        heading,
        groundSpeed,
        verticalSpeed,
        instructedHeading,
        instructedGroundSpeed,
        instructedAltitude,
        type,
        originIata,
        originIcao,
        destinationIata,
        destinationIcao,
        eta
      ] = matches;
      const coordinateOnCanvas = CoordinateManager.calculateCanvasCoordinates(
        parseFloat(lat),
        parseFloat(lon)
      );
      return new Aircraft(
        callsign,
        {
          x: coordinateOnCanvas.x,
          y: coordinateOnCanvas.y,
          altitude: parseFloat(altitude)
        },
        // position
        {
          heading: parseFloat(heading),
          groundSpeed: parseFloat(groundSpeed),
          verticalSpeed: parseFloat(verticalSpeed)
        },
        // vector
        {
          heading: parseFloat(instructedHeading),
          groundSpeed: parseFloat(instructedGroundSpeed),
          altitude: parseFloat(instructedAltitude)
        },
        // instructedVector
        type,
        originIata,
        originIcao,
        destinationIata,
        destinationIcao,
        eta
      );
    }
    throw new Error("Failed to parse aircraft string: " + aircraftString);
  };
  function updateControllingAircrafts(apiResponse, controllingAircrafts) {
    const newAircraftMap = /* @__PURE__ */ new Map();
    apiResponse.forEach((aircraft) => {
      newAircraftMap.set(aircraft.callsign, aircraft);
    });
    controllingAircrafts = controllingAircrafts.filter(
      (airplane) => {
        const newAircraft = newAircraftMap.get(airplane.callsign);
        if (newAircraft) {
          airplane.updateAircraftInfo(newAircraft);
          newAircraftMap.delete(airplane.callsign);
          return true;
        } else {
          return false;
        }
      }
    );
    newAircraftMap.forEach((newAircraft) => {
      const newAirplane = new Aircraft(
        newAircraft.callsign,
        newAircraft.position,
        newAircraft.vector,
        newAircraft.instructedVector,
        newAircraft.type,
        newAircraft.originIata,
        newAircraft.originIcao,
        newAircraft.destinationIata,
        newAircraft.destinationIcao,
        newAircraft.eta
      );
      controllingAircrafts.push(newAirplane);
    });
    return controllingAircrafts;
  }

  // Frontend/scripts/api/controlAircraft.ts
  async function controlAircraft(callsign) {
    const inputAltitude = document.getElementById("altitude");
    const inputHeading = document.getElementById("heading");
    const inputGroundSpeed = document.getElementById("groundSpeed");
    const instructedAltitude = Number(inputAltitude.value);
    const instructedGroundSpeed = Number(inputGroundSpeed.value);
    const instructedHeading = Number(inputHeading.value);
    const controlAircraftDto = {
      instructedAltitude,
      instructedGroundSpeed,
      instructedHeading
    };
    try {
      const response = await fetch(
        `http://localhost:8080/api/aircraft/control/${callsign}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(controlAircraftDto)
        }
      );
      if (response.ok) {
        console.log(`Aircraft ${callsign} controlled successfully.`);
      } else {
        console.error(
          `Failed to control aircraft ${callsign}. Status:`,
          response.status
        );
      }
    } catch (error) {
      console.error("Error occurred while controlling aircraft:", error);
    }
  }

  // Frontend/scripts/aircraft/drawAircraft.ts
  var DrawAircraft = class {
    static drawAircraft(ctx, aircraft) {
      this.drawAircraftMarker(ctx, aircraft.position);
      this.drawHeadingLine(ctx, aircraft.position, aircraft.vector.groundSpeed, aircraft.vector.heading);
      this.drawAircraftLabel(ctx, aircraft);
      this.drawLabelLiine(ctx, aircraft.position, aircraft.label);
    }
    static drawAircraftMarker(ctx, position) {
      const radius = 5;
      ctx.beginPath();
      ctx.arc(
        position.x,
        position.y,
        radius,
        0,
        2 * Math.PI
      );
      ctx.fillStyle = "white";
      ctx.fill();
    }
    static drawHeadingLine(ctx, position, groundSpeed, heading) {
      const futurePosition = CoordinateManager_default.calculateFuturePositionOnCanvas(
        groundSpeed,
        heading,
        GLOBAL_SETTINGS.canvasWidth,
        GLOBAL_SETTINGS.canvasHeight,
        GLOBAL_SETTINGS.displayRange,
        position
      );
      ctx.beginPath();
      ctx.moveTo(position.x, position.y);
      ctx.lineTo(futurePosition.futureX, futurePosition.futureY);
      ctx.strokeStyle = "white";
      ctx.stroke();
    }
    static drawAircraftLabel(ctx, aircraft) {
      const airplanePosition = aircraft.position;
      const labelX = airplanePosition.x + aircraft.label.x;
      const labelY = airplanePosition.y - aircraft.label.y;
      ctx.fillStyle = "white";
      ctx.font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
      ctx.textAlign = "left";
      ctx.fillText(aircraft.callsign, labelX, labelY);
      ctx.fillText(
        Math.floor(airplanePosition.altitude / 100).toString(),
        labelX,
        labelY + 15
      );
      ctx.fillText(
        "G" + Math.floor(aircraft.vector.groundSpeed / 10).toString(),
        labelX,
        labelY + 30
      );
      ctx.fillText(
        aircraft.destinationIata,
        labelX + 40,
        labelY + 30
      );
    }
    static drawLabelLiine(ctx, aircraftPosition, labelPosition) {
      const labelX = aircraftPosition.x + labelPosition.x;
      const labelY = aircraftPosition.y - labelPosition.y;
      const labelDistance = Math.sqrt(Math.pow(labelPosition.x, 2) + Math.pow(labelPosition.y, 2));
      const sin = labelPosition.y / labelDistance;
      const cos = labelPosition.x / labelDistance;
      ctx.beginPath();
      ctx.moveTo(aircraftPosition.x + 10 * cos, aircraftPosition.y - 10 * sin);
      ctx.lineTo(labelX - 5, labelY + 15);
      ctx.strokeStyle = "white";
      ctx.stroke();
    }
  };

  // Frontend/scripts/index.ts
  var RadarGame = class {
    updateInterval;
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
    controllingAircratfts = [];
    displayingWaypoints = [];
    draggingLabelIndex = -1;
    // Index of the label being dragged
    offsetX = 0;
    offsetY = 0;
    selectedAircraft = null;
    //選択中の航空機を保持するための変数
    /**
     * Initializes a new instance of the RadarGame class.
     */
    constructor() {
      this.canvas = [this.createCanvas("radar"), this.createCanvas("radar2")];
      this.ctx = this.canvas.map(
        (c) => c.getContext("2d")
      );
      this.clickedPosition = null;
      this.displayCallsign = document.getElementById("callsign");
      this.inputAltitude = document.getElementById("altitude");
      this.inputSpeed = document.getElementById("groundSpeed");
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
      this.initializeAtsRouteData();
    }
    atsRouteData;
    async initializeAtsRouteData() {
      this.atsRouteData = await atsRoutesLoader_default();
    }
    /**
     * Creates a canvas element with the specified id.
     * @param id - The id of the canvas element.
     * @returns The created HTMLCanvasElement.
     */
    createCanvas(id) {
      const canvas = document.getElementById(id);
      if (!canvas) {
        throw new Error(`Canvas element with id "${id}" not found.`);
      }
      return canvas;
    }
    /**
     * Clears the canvas at the specified index.
     * @param index - The index of the canvas to clear.
     */
    clearCanvas(index) {
      this.ctx[index].fillStyle = "black";
      this.ctx[index].fillRect(0, 0, GLOBAL_SETTINGS.canvasWidth, GLOBAL_SETTINGS.canvasHeight);
    }
    /**
     * Gets a mouse click event and returns the aircraft closest to the clicked location.
     * @param event - The mouse click event.
     */
    onMouseDown(event) {
      const canvas = event.target;
      const rect = canvas.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      const aircraftRadius = 30;
      for (const [index, airplane] of this.controllingAircratfts.entries()) {
        const { position, label, callsign } = airplane;
        const labelX = position.x + label.x;
        const labelY = position.y - label.y;
        if (this.isWithinRadius(x, y, position, aircraftRadius)) {
          this.changeDisplayCallsign(callsign);
          this.inputAltitude.value = airplane.instructedVector.altitude.toString();
          this.inputSpeed.value = airplane.instructedVector.groundSpeed.toString();
          this.inputHeading.value = airplane.instructedVector.heading.toString();
          this.selectedAircraft = airplane;
          break;
        }
        if (this.isWithinLabelBounds(x, y, labelX, labelY)) {
          this.draggingLabelIndex = index;
          this.offsetX = x - labelX;
          this.offsetY = y - labelY;
          break;
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
    isWithinRadius(x, y, position, radius) {
      return x >= position.x - radius && x <= position.x + radius && y >= position.y - radius && y <= position.y + radius;
    }
    /**
     * Checks if the click is within the bounds of the label.
     * @param x - Clicked x-coordinate.
     * @param y - Clicked y-coordinate.
     * @param labelX - Label x-coordinate.
     * @param labelY - Label y-coordinate.
     * @returns True if the click is within the label bounds, otherwise false.
     */
    isWithinLabelBounds(x, y, labelX, labelY) {
      return x >= labelX - 5 && x <= labelX + 70 && y >= labelY - 20 && y <= labelY + 40;
    }
    /**
     * Handles the mouse move event to update the position of a dragged label.
     * @param event - The mouse move event.
     */
    onMouseMove(event) {
      if (this.draggingLabelIndex === -1) return;
      const canvas = event.target;
      const rect = canvas.getBoundingClientRect();
      const mouseX = event.clientX - rect.left;
      const mouseY = event.clientY - rect.top;
      const aircraft = this.controllingAircratfts[this.draggingLabelIndex];
      const label = aircraft.label;
      const aircraftPosition = aircraft.position;
      label.x = mouseX - this.offsetX - aircraftPosition.x;
      label.y = aircraftPosition.y - (mouseY - this.offsetY);
    }
    /**
     * Handles the mouse up event.
     */
    onMouseUp() {
      this.draggingLabelIndex = -1;
    }
    /**
     * Changes the displayed callsign.
     * @param newCallsign - The new callsign to display.
     */
    changeDisplayCallsign(newCallsign) {
      const fontElement = document.getElementById(
        "callsign"
      );
      if (fontElement) {
        fontElement.textContent = newCallsign;
      }
    }
    toggleCanvasDisplay() {
      this.canvas[1 - this.bg].style.display = "none";
      this.canvas[this.bg].style.display = "block";
      this.bg = 1 - this.bg;
    }
    async update() {
      this.clearCanvas(this.bg);
      renderMap(this.atsRouteData.waypoints, this.atsRouteData.radioNavigationAids, this.atsRouteData.atsLowerRoutes, this.atsRouteData.rnavRoutes, this.ctx[this.bg]);
      for (let i = 0; i < this.controllingAircratfts.length; i++) {
        DrawAircraft.drawAircraft(this.ctx[this.bg], this.controllingAircratfts[i]);
      }
      this.toggleCanvasDisplay();
    }
    async makeFirstCanvas() {
      if (!this.ctx[0] || !this.ctx[1]) {
        console.error("Failed to get 2D context");
        return;
      }
    }
    async start() {
      this.updateInterval = setInterval(() => {
        this.update();
      }, 1e3 / GLOBAL_CONSTANTS.REFRESH_RATE);
    }
    stop() {
      if (this.updateInterval) {
        clearInterval(this.updateInterval);
        this.updateInterval = null;
        console.log("Game stopped");
      }
    }
  };
  var radarGame = new RadarGame();
  radarGame.makeFirstCanvas();
  atsRoutesLoader_default().then((data) => {
    console.log(data);
  }).catch((error) => {
    console.error(error);
  });
  var startButton = document.getElementById("startButton");
  startButton?.addEventListener("click", () => {
    radarGame.start();
    console.log("Game started");
    if (!GLOBAL_SETTINGS.locationUpdateInterval) {
      GLOBAL_SETTINGS.locationUpdateInterval = setInterval(async () => {
        radarGame.controllingAircratfts = await fetchAircraftLocation(radarGame.controllingAircratfts);
      }, 1e3);
    }
  });
  var stopButton = document.getElementById("stopButton");
  stopButton?.addEventListener("click", () => {
    radarGame.stop();
    if (GLOBAL_SETTINGS.locationUpdateInterval) {
      clearInterval(GLOBAL_SETTINGS.locationUpdateInterval);
      GLOBAL_SETTINGS.locationUpdateInterval = null;
    }
  });
  var confirmButton = document.getElementById("confirmButton");
  confirmButton?.addEventListener("click", () => {
    if (radarGame.selectedAircraft) {
      console.log("Confirm button clicked");
      controlAircraft(radarGame.selectedAircraft.callsign);
    } else {
      console.error("No aircraft selected");
    }
  });
  var displayRangeElement = document.getElementById("displayRange");
  displayRangeElement?.addEventListener("input", (event) => {
    const newRange = parseFloat(event.target.value);
    GLOBAL_SETTINGS.displayRange = newRange;
  });
  var resetButton = document.getElementById("resetButton");
  var handleResetButtonClick = async () => {
    console.log("Reset button clicked");
    try {
      const response = await fetch("http://localhost:8080/hello/", {
        method: "GET",
        headers: {
          "accept": "*/*"
        }
      });
      if (response.ok) {
        const data = await response.text();
        console.log("Response Data:", data);
      } else {
        console.error("Request failed with status:", response.status);
      }
    } catch (error) {
      console.error("Error occurred:", error);
    }
  };
  resetButton?.addEventListener("click", handleResetButtonClick);
  var settingsMap = {
    "waypoint-name": "isDisplayingWaypointName",
    "waypoint-point": "isDisplayingWaypointPoint",
    "radio-navigation-aids-name": "isDisplayingRadioNavigationAidsName",
    "radio-navigation-aids-point": "isDisplayingRadioNavigationAidsPoint",
    "ats-lower-routes": "isDisplayingAtsLowerRoute",
    "rnav-routes": "isDisplayingRnavRoute"
  };
  function handleCheckboxChange(settingKey) {
    return (event) => {
      const isChecked = event.target.checked;
      GLOBAL_SETTINGS[settingKey] = isChecked;
    };
  }
  Object.keys(settingsMap).forEach((checkboxId) => {
    const checkBoxElement = document.getElementById(checkboxId);
    const settingKey = settingsMap[checkboxId];
    checkBoxElement?.addEventListener("change", handleCheckboxChange(settingKey));
  });
  var sectorCenterCoordinates = {
    "T09": { latitude: 34.482083333333335, longitude: 138.61388888888888 },
    "T10": { latitude: 33.04138888888889, longitude: 139.4561111111111 },
    "T14": { latitude: 33.66722222222222, longitude: 137.91833333333335 },
    "T25": { latitude: 34.54944444444445, longitude: 136.96555555555557 },
    "T30": { latitude: 43.29444444444445, longitude: 142.67916666666667 },
    "T31": { latitude: 41.85805555555555, longitude: 140.1590277777778 },
    "T32": { latitude: 40.209722222222226, longitude: 141.23722222222221 },
    "T33": { latitude: 38.474999999999994, longitude: 138.8048611111111 },
    "T34": { latitude: 37.543055555555554, longitude: 141.55124999999998 },
    "T35": { latitude: 36.84736111111111, longitude: 139.40930555555553 },
    "T36": { latitude: 35.77388888888889, longitude: 142.14041666666665 },
    "T38": { latitude: 35.827083333333334, longitude: 139.15763888888887 },
    "T39": { latitude: 35.273472222222225, longitude: 139.24930555555557 },
    "T45": { latitude: 34.30236111111111, longitude: 135.53097222222223 },
    "T46": { latitude: 33.43291666666667, longitude: 135.91680555555556 },
    "T92": { latitude: 38.30972222222222, longitude: 141.12583333333333 },
    "T93": { latitude: 36.28833333333333, longitude: 142.9763888888889 },
    "F01": { latitude: 42.377361111111114, longitude: 141.8722222222222 },
    "F04": { latitude: 39.608194444444436, longitude: 136.90722222222223 },
    "F05": { latitude: 35.63972222222222, longitude: 138.83125 },
    "F07": { latitude: 36.840694444444445, longitude: 135.81527777777777 },
    "F08": { latitude: 35.93208333333333, longitude: 132.54180555555553 },
    "F09": { latitude: 33.681805555555556, longitude: 134.32638888888889 },
    "F10": { latitude: 32.59625, longitude: 134.68847222222223 },
    "F11": { latitude: 31.687083333333334, longitude: 135.36013888888888 },
    "F12": { latitude: 33.90861111111111, longitude: 131.05680555555557 },
    "F13": { latitude: 31.779722222222222, longitude: 127.51916666666668 },
    "F14": { latitude: 31.530277777777776, longitude: 130.745 },
    "F15": { latitude: 28.71666666666667, longitude: 126.49805555555555 },
    "F16": { latitude: 28.601805555555558, longitude: 129.5948611111111 },
    "F17": { latitude: 24.36763888888889, longitude: 126.57083333333333 },
    "N43": { latitude: 36.96819444444444, longitude: 137.32430555555555 },
    "N44": { latitude: 35.09638888888889, longitude: 136.40041666666667 },
    "N47": { latitude: 35.17722222222223, longitude: 135.38680555555555 },
    "N48": { latitude: 36.169444444444444, longitude: 134.56944444444446 },
    "N49": { latitude: 34.59513888888889, longitude: 134.17916666666667 },
    "N50": { latitude: 33.25541666666667, longitude: 133.79736111111112 },
    "N51": { latitude: 34.531111111111116, longitude: 131.56319444444443 },
    "N52": { latitude: 33.38305555555556, longitude: 132.2573611111111 },
    "N53": { latitude: 33.26736111111111, longitude: 129.81375000000003 },
    "N54": { latitude: 31.617222222222225, longitude: 131.49763888888887 },
    "N55": { latitude: 28.23111111111111, longitude: 128.70791666666668 },
    "A01": { latitude: 44.36263888888889, longitude: 151.79333333333335 },
    "A02": { latitude: 42.24486111111111, longitude: 154.02319444444444 },
    "A03": { latitude: 35.25888888888889, longitude: 153.5966666666667 },
    "A04": { latitude: 27.654166666666665, longitude: 143.64499999999998 },
    "A05": { latitude: 24.915694444444444, longitude: 132.88125 }
  };
  var sectorSelecter = document.getElementById("selectSector");
  sectorSelecter?.addEventListener("change", (event) => {
    const selectedSector = event.target.value;
    console.log("Selected sector:", selectedSector);
    GLOBAL_SETTINGS.centerCoordinates = sectorCenterCoordinates[selectedSector];
  });
})();
