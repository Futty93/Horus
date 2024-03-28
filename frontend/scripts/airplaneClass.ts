const airlines: string[] = ["ANA", "JAL", "APJ", "IBX", "SFJ", "SKY", "FDA", "ADO", "SNJ", "AHX"];
const CALLSIGN: string[] = ["all nippon", "japan air", "air peach", "ibex", "star flyer", "sky mark", "fuji dream", "air do", "new sky", "amakusa air"] 
const destAirport: string[] = [
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
  "YGJ",
];

const updateRange = 50;

/**
 * 航空機の情報の保持、更新に関するクラス
 * @param {string} callsign 初期値を渡さなかった場合ランダムなコールサインを生成
 * @param {number} altitude 高度を保持する変数。初期値を与えなかったまたは不正な値だった場合、ランダムな高度を生成
 * @param {number} commandedAltitude 指示された高度を保持する変数。初期値はaltitudeと同じ
 * @param location 航空機の現在位置を保持する変数。初期値を与えなかった場合はランダムな座標を生成
 * @param {number} heading 進行方向を保持する変数。初期値を与えなかった場合はランダムな進行方向を生成
 * @param {number} commandedHeading 指示された進行方向を保持する変数。初期値はheadingと同じ
 * @param {number} speed 速度を保持する変数。初期値を与えなかったまたは不正な値だった場合、ランダムな速度を生成
 * @param {number} commandedSpeed 指示された速度を保持する変数。初期値はspeedと同じ
 * @param {string} destination 目的地となる空港の3レターを保持する変数。初期値を与えなかった場合ランダムな目的地を生成
 * @param labelLocation ラベルを表示する位置を保持する変数。初期値では航空機の右上50px。航空機の表示位置からの相対座標。
 */

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
  private draggingLabelIndex: number;

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
      x: 50,
      y: 50,
    };
    this.draggingLabelIndex = -1;
  }

  /**
   * コールサインとして適切なものが入力されているかどうかを判定する関数
   * @param callsign 
   * @returns {boolean} callsingが入力されており、かつ""でない時にtrueを返す
   */
  private isCallsignValid(callsign: string | undefined): callsign is string {
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
    return altitude !== undefined && (altitude >= 0 && altitude < 500);
  }

  private getRandomAltitude(): number {
    //0以上500以下の整数を返す
    return Math.floor(Math.random() * 490 + 10);
  }

  private isHeadingValid(heading: number | undefined): heading is number {
    //Headingが指定されているかつ0以上36以下である場合はtrueを返す
    return heading !== undefined && (heading >= 0 && heading < 36);
  }

  private getRandomHeading(): number {
    return Math.floor(Math.random() * 35);
  }

  private isSpeedValid(speed: number | undefined): speed is number {
    //速度が引数で指定されているかつ常識の範囲内である場合はtrueを返す
    return speed !== undefined && (speed >= 100 && speed < 500);
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
  updateLocation(refleshRate: number): void {
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
  calculateSpeedComponents(
    speed: number,
    heading: number,
  ): SpeedVector {
    if (heading < 0 || heading > 36) {
      throw new Error("Invalid heading. Heading should be between 0 and 36.");
    }

    // Convert heading to radians
    const radians = (heading * 10 * Math.PI) / 180;

    // Calculate the x and y components of the speed
    const xSpeed = Math.sin(radians) * speed / updateRange;
    const ySpeed = -Math.cos(radians) * speed / updateRange;

    return new SpeedVector(xSpeed, ySpeed);
  }

  currentPosition(): { currentX: number; currentY: number } {
    //画面を更新する時に呼び出し、現在の航空機の座標を返す。
    const currentX = this.location.positionX;
    const currentY = this.location.positionY;
    return { currentX, currentY };
  }

  getAirplaneInfo(): {
    callsign: string;
    heading: string;
    speed: string;
    altitude: string;
    commandedHeading: string;
    commandedSpeed: string;
    commandedAltitude: string;
    destination: string;
    labelLocation: {x: number, y: number}
  } {
    //ラベルの表示など必要なときに航空機の情報を返す
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

  changeCommandedInfo(
    inputAltitude: number,
    inputSpeed: number,
    inputHeading: number,
  ): { newAltitude: string; newSpeed: string; newHeading: string } {
    const newAltitude: number = this.isAltitudeValid(inputAltitude)
      ? inputAltitude
      : this.altitude;
    this.commandedAltitude = newAltitude;
    const newSpeed: number = this.isSpeedValid(inputSpeed)
      ? inputSpeed
      : this.speed;
    this.commandedSpeed = newSpeed;
    const newHeading: number = this.isHeadingValid(inputHeading)
      ? inputHeading
      : this.heading;
    this.commandedHeading = newHeading;

    //将来的に徐々に変化するように変更したい
    this.altitude = this.commandedAltitude;
    this.speed = this.commandedSpeed;
    this.heading = this.commandedHeading;

    return {
      newAltitude: String(newAltitude),
      newSpeed: String(newSpeed),
      newHeading: String(newHeading),
    };
  }
}

class SpeedVector {
  constructor(public xSpeed: number, public ySpeed: number) {}
}