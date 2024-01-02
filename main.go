package main

import (
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

func main() {

	// サーバーを作成
	msg.Start()
	messenger := msg.Messenger{}
	messenger.CreateMessageServer()
}
