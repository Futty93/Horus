package main

import (
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/aircraft"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

func main() {
	a := aircraft.CommercialAircraft{}
	a = a.NewTestCommercialAircraft()
	// クライアントと通信するサーバーを作成
	msg.Start()
}
