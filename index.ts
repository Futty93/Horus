// Define constants for canvas dimensions
const CANVAS_WIDTH: number = 1000;
const CANVAS_HEIGHT: number = 1000;
const REFRESH_RATE: number = 30; //画面の更新頻度(fps)

import { Airplane } from "./airplaneClass";

// Define a class to encapsulate the game
class RadarGame {
  private canvas: HTMLCanvasElement[]; //ダブルバッファで画面を切り替えてアニメーションを実現するために配列で定義
  private ctx: CanvasRenderingContext2D[];
  private inGame: boolean; //シミュレーションゲーム中かどうかを判断する
  private bg: number; //ダブルバッファの背景と表示を切り替えるためのインデックスを管理
  private positionX: number;
  private positionY: number;
  private controlledAirplane: Airplane[] = [];

  constructor() {
    //初期変数を初期化する
    this.canvas = [this.createCanvas("radar"), this.createCanvas("radar2")];
    this.ctx = this.canvas.map((c) => c.getContext("2d") as CanvasRenderingContext2D);
    this.inGame = false;
    this.bg = 0;
    this.positionX = 400;
    this.positionY = 600;
    //とりあえず10機の航空機を生成する
    for (let i=1; i <= 10; i++) {
        const airplane = new Airplane();
        this.controlledAirplane.push(airplane);
    }
  }

  private createCanvas(id: string): HTMLCanvasElement {
    //HTMLからキャンバスを取得してきて、もし取得できなければエラーを出力
    const canvas = document.getElementById(id) as HTMLCanvasElement;
    if (!canvas) {
      throw new Error(`Canvas element with id "${id}" not found.`);
    }
    return canvas;
  }

  private clearCanvas(index: number): void {
    //ダブルバッファで新しい画面を描画する前に一旦消す
    this.ctx[index].fillStyle = "black"
    this.ctx[index].fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  }

  private updatePosition(airplane: Airplane): void {
    //航空機の速度に合わせてポジションを更新する
    airplane.updateLocation();
  }

  private drawRect(index: number, airplane: Airplane): void {
    //航空機のポジションに描画する
    const position = airplane.currentPosition();
    this.ctx[index].beginPath();
    this.ctx[index].rect(position.currentX, position.currentY, 5, 5);
    this.ctx[index].fillStyle = "white";
    this.ctx[index].fill();
  }

  private toggleCanvasDisplay(): void {
    //ダブルバッファの表示するキャンバスを切り替える
    this.canvas[1 - this.bg].style.display = "none";
    this.canvas[this.bg].style.display = "block";
    this.bg = 1 - this.bg;
  }

  private update(): void {
    //画面全体を更新する
    this.clearCanvas(this.bg);
    for (let i=0; i < this.controlledAirplane.length; i++) {
        this.updatePosition(this.controlledAirplane[i]);
        this.drawRect(this.bg, this.controlledAirplane[i]);
    }
    this.toggleCanvasDisplay();
  }

  public start(): void {
    if (!this.ctx[0] || !this.ctx[1]) {
      console.error("Failed to get 2D context");
      return;
    }

    setInterval(() => {
      this.update();
    }, 1000 / REFRESH_RATE);
  }
}

// Initialize and start the game
const radarGame = new RadarGame();
radarGame.start();
