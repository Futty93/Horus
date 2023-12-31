package main

import (
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/aircraft"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

func main() {
	a := aircraft.NewAircraft([2]float64{1.0, 1.0}, 1)
	a.PrintPosition()

	// サーバーを作成
	messenger := msg.Messenger{}
	messenger.CreateMessageServer()
}
