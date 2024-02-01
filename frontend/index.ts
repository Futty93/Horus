// Define constants for canvas dimensions
const CANVAS_WIDTH: number = 1000;
const CANVAS_HEIGHT: number = 1000;
const REFRESH_RATE: number = 0.1; //画面の更新頻度(fps)

import { Airplane } from "./airplaneClass.ts";

// Define a class to encapsulate the game
class RadarGame {
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
  private controlledAirplane: Airplane[] = [];

  private selectedAircraft: Airplane | null = null; //選択中の航空機を保持するための変数

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
    // this.sendButton = <HTMLInputElement> document.getElementById("sendButton");
    this.inGame = false;
    this.bg = 0;
    this.canvas[0].addEventListener("click", (e) => this.handleClick(e));
    this.canvas[1].addEventListener("click", (e) => this.handleClick(e));
    this.confirmButton.addEventListener("click", () => this.send_command());
    // this.sendButton.addEventListener("click", () => sendToServer());

    //とりあえず10機の航空機を生成する
    for (let i = 1; i <= 10; i++) {
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
    this.ctx[index].fillStyle = "black";
    this.ctx[index].fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  }

  private updatePosition(airplane: Airplane): void {
    //航空機の速度に合わせてポジションを更新する
    airplane.updateLocation();
  }

  private handleClick(event: MouseEvent): void {
    // Get the clicked position relative to the canvas
    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Store the clicked position
    this.clickedPosition = { x, y };

    // Iterate through the aircraft to check if they are near the clicked position
    for (const airplane of this.controlledAirplane) {
      const position = airplane.currentPosition();
      const aircraftRadius = 50; // Adjust the radius as needed

      // Check if the clicked position is near the aircraft's position
      if (
        x >= position.currentX - aircraftRadius &&
        x <= position.currentX + aircraftRadius &&
        y >= position.currentY - aircraftRadius &&
        y <= position.currentY + aircraftRadius
      ) {
        // Log the aircraft information to the console
        const airplaneInfo = airplane.getAirplaneInfo();
        this.changeDisplayCallsign(airplaneInfo.callsign);
        this.inputAltitude.value = airplaneInfo.commandedAltitude;
        this.inputSpeed.value = airplaneInfo.commandedSpeed;
        this.inputHeading.value = airplaneInfo.commandedHeading;
        //選ばれている航空機を保持する
        this.selectedAircraft = airplane;

        break;
      }
    }
  }

  //表示しているコールサインを変更する
  private changeDisplayCallsign(newCallsign: string): void {
    const fontElement = <HTMLParagraphElement>document.getElementById("callsign");
    if (fontElement) {
      fontElement.textContent = newCallsign;
    }
  }
  

  //航空機およびデータラベルの表示
  private drawRect(index: number, airplane: Airplane): void {
    const position = airplane.currentPosition();
    const airplaneInfo = airplane.getAirplaneInfo();
    const labelX = airplaneInfo.labelX;
    const labelY = airplaneInfo.labelY;
    const radius: number = 5; //航空機のノードの半径

    //航空機のポジションに描画する
    this.ctx[index].beginPath();
    this.ctx[index].arc(position.currentX, position.currentY, radius, 0, 2 * Math.PI); //原点を中心に、半径 radius の円弧を描画する
    this.ctx[index].fillStyle = "white";
    this.ctx[index].fill();

    // ラベルと航空機を線で結ぶ
    this.ctx[index].beginPath();
    this.ctx[index].moveTo(
      (position.currentX * 8 + labelX * 2) / 10,
      (position.currentY * 8 + labelY * 2) / 10,
    ); // 航空機から少し離れたところから始める
    this.ctx[index].lineTo(labelX - 5, labelY + 15); // 高度のあたりに終点が来るようにする
    this.ctx[index].strokeStyle = "white";
    this.ctx[index].stroke();

    // Display aircraft information at labelLocation
    this.ctx[index].fillStyle = "white";
    this.ctx[index].font = "12px Arial";
    this.ctx[index].textAlign = "left";

    // Display callsign on the first line
    this.ctx[index].fillText(airplaneInfo.callsign, labelX, labelY);

    // Display altitude on the second line
    this.ctx[index].fillText(airplaneInfo.altitude, labelX, labelY + 15);

    // Display heading and speed on the third line
    this.ctx[index].fillText(airplaneInfo.speed, labelX, labelY + 30);
    this.ctx[index].fillText(
      airplaneInfo.destination,
      labelX + 40,
      labelY + 30,
    );
  }

  private toggleCanvasDisplay(): void {
    //ダブルバッファの表示するキャンバスを切り替える
    this.canvas[1 - this.bg].style.display = "none";
    this.canvas[this.bg].style.display = "block";
    this.bg = 1 - this.bg;
  }

  private send_command(): void {
    if (this.selectedAircraft) {
        const { newAltitude, newSpeed, newHeading } = this.selectedAircraft.changeCommandedInfo(
            Number(this.inputAltitude.value),
            Number(this.inputSpeed.value),
            Number(this.inputHeading.value)
          );
      
          // Now you have the extracted values in newAltitude, newSpeed, and newHeading
          this.inputAltitude.value = newAltitude;
          this.inputSpeed.value = newSpeed;
          this.inputHeading.value = newHeading;
    }
    console.log(this.selectedAircraft);
  }

  private send_to_server(): void {
    // データを準備（例: JSONデータ）
    const dataToSend = { key: 'value' };

    // fetchを使ってサーバーにデータを送信
    fetch('http://localhost:8080', {
        method: 'POST', // 送信方法（POSTなど）
        headers: {
            'Content-Type': 'application/json', // 送信するデータの種類
            // サーバー側にCORSリクエストを送るためのヘッダーを追加
            'Access-Control-Allow-Origin': '*', // サーバーの設定によって適切な値に変更
        },
        body: JSON.stringify(dataToSend), // 送信するデータをJSON文字列に変換
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok.');
        }
        return response.json(); // サーバーからのレスポンスをJSONとして処理
    })
    .then(data => {
        console.log('Server response:', data); // サーバーからのレスポンスをログに出力
        // ここでサーバーからのレスポンスを適切に処理する（UIに反映するなど）
    })
    .catch(error => {
        console.error('There was a problem with the fetch operation:', error); // エラーをログに出力
        // エラー処理を行う（例: エラーメッセージをユーザーに表示するなど）
    });
  }

  private update(): void {
    //画面全体を更新する
    this.clearCanvas(this.bg);
    for (let i = 0; i < this.controlledAirplane.length; i++) {
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
    this.update(); //最初期の画面を表示
    setInterval(() => {
      this.update();
    }, 1000 / REFRESH_RATE);
  }
}

// Initialize and start the game
const radarGame = new RadarGame();
radarGame.start();


let socket: WebSocket | null = null;

function connectToServer() {
  // サーバーにWebSocket接続を確立
  socket = new WebSocket('ws://localhost:8080/ws');

  // 接続が確立されたときの処理
  socket.addEventListener('open', (event) => {
      console.log('Connected to server');
  });

  // メッセージを受信したときの処理
  socket.addEventListener('message', (event) => {
      console.log('Received message from server:', event.data);
      // サーバーからのメッセージを適切に処理する（UIに反映するなど）
  });

  // エラーが発生したときの処理
  socket.addEventListener('error', (event) => {
      console.error('Error with WebSocket connection:', event);
      // エラー処理を行う（例: エラーメッセージをユーザーに表示するなど）
  });

  // 接続が閉じられたときの処理
  socket.addEventListener('close', (event) => {
      console.log('Connection closed');
  });
}

function sendToServer() {
  if (socket && socket.readyState === WebSocket.OPEN) {
      const dataToSend = { key: 'value' };
      // サーバーにデータを送信
      socket.send(JSON.stringify(dataToSend));
      console.log("send!")
  } else {
      console.error('WebSocket connection is not open');
      // WebSocket接続が開かれていない場合のエラー処理
  }
}

// サーバーに接続
connectToServer();