package main

import (
	tea "github.com/charmbracelet/bubbletea"
	"log"
	"sync"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/game"
)

func main() {
	// create game
	g := game.Game(game.GetManagerInstance())
	g.Start()
	g.Next()

	// wait group
	var wg sync.WaitGroup

	wg.Add(1)
	// create console program
	p := tea.NewProgram(initialModel())
	go func() {
		_, err := p.Run()
		if err != nil {
			log.Fatal(err)
		}
	}()

	// waiting for ends each process
	wg.Wait()
}
