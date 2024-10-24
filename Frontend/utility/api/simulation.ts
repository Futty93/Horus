export class SimulationManager {
  constructor() {
    const startButton: HTMLImageElement = document.getElementById("startButton") as HTMLImageElement;
    const pauseButton: HTMLImageElement = document.getElementById("pauseButton") as HTMLImageElement;

    startButton?.addEventListener("click", () => this.handleSimulationAction("start"));
    pauseButton?.addEventListener("click", () => this.handleSimulationAction("pause"));
  }

  // シミュレーションの開始・一時停止を処理する共通メソッド
  private async handleSimulationAction(action: "start" | "pause"): Promise<void> {
    const url = `http://localhost:8080/simulation/${action}`;
    try {
      const response = await fetch(url, {
        method: "POST",  // メソッドをPOSTに変更
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (response.ok) {
        console.log(`${action.charAt(0).toUpperCase() + action.slice(1)} successful`);
      } else {
        console.error(`Failed to ${action} simulation. Status:`, response.status);
      }
    } catch (error) {
      console.error(`Error occurred while trying to ${action} simulation:`, error);
    }
  }
}