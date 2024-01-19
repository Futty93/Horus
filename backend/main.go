package main

import (
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/game"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

func main() {
	g := game.Game(game.GetManagerInstance())
	g.Start()
	g.LogSnapShot()
	g.Next()
	g.LogSnapShot()
	// クライアントと通信するサーバーを作成
	msg.Start()
}
