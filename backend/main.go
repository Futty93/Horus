package main

import (
	tea "github.com/charmbracelet/bubbletea"
	"log"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/game"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

func main() {
	// create game
	g := game.Game(game.GetManagerInstance())
	g.Start()
	g.Next()

	// クライアントと通信するサーバーを作成
	msg.Start()

	// create console program
	p := tea.NewProgram(initialModel())
	_, err := p.Run()
	if err != nil {
		log.Fatal(err)
	}
}
