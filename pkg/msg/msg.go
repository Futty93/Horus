// Package msg provides messaging server to client app.
package msg

import (
	"fmt"
	"github.com/gorilla/websocket"
	"net/http"
)

// Messenger is server for client.
type Messenger struct {
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		// クロスオリジンリクエストのチェック
		return true
	},
}

// CreateMessageServer initialize the server and endpoint.
func (m Messenger) CreateMessageServer() {
	// WebSocket用のエンドポイントを作成
	http.HandleFunc("/ws", wsTestHandler)
	fmt.Println("Server is listening on port 8080...")
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		fmt.Println("Server couldn't start:", err)
	}
}

// wsTestHandler is for test endpoint handler. It returns the same message and display it.
func wsTestHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		fmt.Println("Error upgrading to WebSocket:", err)
		return
	}
	defer func() {
		if err = conn.Close(); err != nil {
			fmt.Println("Connection close failed")
		}
	}()

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

//
