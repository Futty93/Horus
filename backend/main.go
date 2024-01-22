package main

import (
	tea "github.com/charmbracelet/bubbletea"
	"log"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/game"
)

func main() {
	// create game
	g := game.Game(game.GetManagerInstance())
	g.Start()
	g.Next()

	// create console program
	p := tea.NewProgram(initialModel())

	_, err := p.Run()
	if err != nil {
		log.Fatal(err)
	}

}
