const airlines: string[] = ["ANA", "JAL", "APJ", "IBX", "SFJ", "SKY"];
const destAirport: string[] = ["HND", "NRT", "KIX", "ITM", "CTS", "FUK", "NGO", "OKA", "KOJ", "KMJ", "HIJ", "KOI", "FSZ", "NGS", "KCZ", "TAK", "MYJ", "TOY", "AOJ", "AKJ", "AOJ", "AXT", "GAJ", "HKD", "IZO", "IWJ", "MMB", "OKD", "OIT", "ONJ", "RIS", "SDJ", "SYO", "TNE", "TNA", "UBJ", "MMJ", "KKJ", "YGJ", "AOJ", "AXT", "GAJ", "HKD", "IZO", "IWJ", "MMB", "OKD", "OIT", "ONJ", "RIS", "SDJ", "SYO", "TNE", "TNA", "UBJ", "MMJ", "KKJ", "YGJ"];
  

export class Airplane {
  private callsign: string;
  private altitude: number;
  private commandedAltitude: number;
  private location: { positionX: number; positionY: number };
  private heading: number;
  private commandedHeading: number;
  private speed: number;
  private commandedSpeed: number;
  private destination: string;
  private labelLocation: { x: number; y: number };

  constructor(
    callsign?: string,
    altitude?: number,
    location?: { positionX: number; positionY: number },
    heading?: number,
    speed?: number,
    destination?: string,
  ) {
    this.callsign = this.isCallsignValid(callsign)
      ? callsign
      : this.getRandomCallsing();
    this.altitude = this.isAltitudeValid(altitude)
      ? altitude
      : this.getRandomAltitude();
    this.commandedAltitude = this.altitude;
    this.location = location || this.getRandomLocation();
    this.heading = this.isHeadingValid(heading)
      ? heading
      : this.getRandomHeading();
    this.commandedHeading = this.heading;
    this.speed = this.isSpeedValid(speed) ? speed : this.getRandomSpeed();
    this.commandedSpeed = this.speed;
    this.destination = this.isCallsignValid(destination)
      ? destination
      : this.getRandomDestination();
    this.labelLocation = {
        //デフォルトでは右上に表示
      x: this.location.positionX + 50,
      y: this.location.positionY - 50,
    };
  }

  private isCallsignValid(callsign: string | undefined): callsign is string {
    //callsingが入力されており、かつ""でない時にtrueを返す
    return callsign !== undefined && callsign !== "";
  }

  private getRandomCallsing(): string {
    //ランダムなコールサインを作成
    let randomCallsign: string;
    const randomIndex = Math.floor(Math.random() * airlines.length);
    const randomCallNumber: string = String(
      Math.floor(Math.random() * 2000 + 100),
    );
    randomCallsign = airlines[randomIndex] + randomCallNumber;
    return randomCallsign;
  }

  private isAltitudeValid(altitude: number | undefined): altitude is number {
    //引数が指定されているかつ0以上である場合はtrueを返す
    return altitude !== undefined && altitude >= 0;
  }

  private getRandomAltitude(): number {
    //0以上500以下の整数を返す
    return Math.floor(Math.random() * 490 + 10);
  }

  private isHeadingValid(heading: number | undefined): heading is number {
    //Headingが指定されているかつ0以上36以下である場合はtrueを返す
    return heading !== undefined && (heading >= 0 && 36 < heading);
  }

  private getRandomHeading(): number {
    return Math.floor(Math.random() * 35);
  }

  private isSpeedValid(speed: number | undefined): speed is number {
    //速度が引数で指定されているかつ常識の範囲内である場合はtrueを返す
    return speed !== undefined && (speed >= 100 && 500 < speed);
  }

  private getRandomSpeed(): number {
    //100以上500以下の整数を返す
    return Math.floor(Math.random() * 400 + 100);
  }

  private getRandomLocation(): { positionX: number; positionY: number } {
    return {
      positionX: Math.random() * 1000,
      positionY: Math.random() * 1000,
    };
  }

  private getRandomDestination(): string {
    //ランダムなコールサインを作成
    let randomDestination: string;
    const randomIndex = Math.floor(Math.random() * destAirport.length);
    randomDestination = destAirport[randomIndex];
    return randomDestination;
  }

  // 航空機の表示を操作するための関数↓↓↓↓↓↓↓↓↓↓
  updateLocation(): void {
    const speedComponents = this.calculateSpeedComponents(
      this.speed,
      this.heading,
    );
    this.location.positionX += speedComponents.x / 1000;
    this.location.positionY += speedComponents.y / 1000;
    this.labelLocation.x += speedComponents.x / 1000;
    this.labelLocation.y += speedComponents.y / 1000;
  }

  private calculateSpeedComponents(
    speed: number,
    heading: number,
  ): { x: number; y: number } {
    if (heading < 0 || heading > 36) {
      throw new Error("Invalid heading. Heading should be between 0 and 36.");
    }

    // Convert heading to radians
    const radians = (heading * 10 * Math.PI) / 180;

    // Calculate the x and y components of the speed
    const xSpeed = Math.sin(radians) * speed;
    const ySpeed = -Math.cos(radians) * speed;

    return { x: xSpeed, y: ySpeed };
  }

  currentPosition(): { currentX: number; currentY: number } {
    //画面を更新する時に呼び出し、現在の航空機の座標を返す。
    const currentX = this.location.positionX;
    const currentY = this.location.positionY;
    return { currentX, currentY };
  }

  getAirplaneInfo(): { callsign: string; heading: string; speed: string; altitude: string; destination: string, labelX: number; labelY: number } {
    //ラベルの表示など必要なときに航空機の情報を返す
    const callsign = this.callsign;
    const heading = String(this.heading);
    const speed = String(this.speed);
    const altitude = String(this.altitude);
    const destination = this.destination
    const labelX = this.labelLocation.x
    const labelY = this.labelLocation.y
    return {callsign, heading, speed, altitude, destination, labelX, labelY}
  }
}
