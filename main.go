package main

import (
	"fmt"
	"net/http"

	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		// クロスオリジンリクエストのチェック
		return true
	},
}

func main() {
	// WebSocket用のエンドポイントを作成
	http.HandleFunc("/ws", wsHandler)
	fmt.Println("Server is listening on port 8080...")
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		fmt.Println("Server couldn't start:", err)
	}
}

func wsHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		fmt.Println("Error upgrading to WebSocket:", err)
		return
	}
	defer conn.Close()

	// WebSocket接続の処理
	for {
		// メッセージの読み取り
		_, msg, err := conn.ReadMessage()
		if err != nil {
			fmt.Println("Error reading message:", err)
			break
		}
		fmt.Printf("Received message: %s\n", msg)

		// メッセージの書き込み
		if err := conn.WriteMessage(websocket.TextMessage, msg); err != nil {
			fmt.Println("Error writing message:", err)
			break
		}
	}
}
