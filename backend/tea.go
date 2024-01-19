package main

import (
	"fmt"
	tea "github.com/charmbracelet/bubbletea"
)

type model struct {
	count int
}

func initialModel() model {
	return model{count: 0}
}

func (model) Init() tea.Cmd {
	return nil
}

func (m model) View() string {
	return fmt.Sprintf("count: %v", m.count)
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.KeyMsg:
		switch msg.String() {
		case " ":
			m.count += 1
			return m, nil
		case "q", "ctl+c":
			return m, tea.Quit
		}
	}
	return m, nil
}
