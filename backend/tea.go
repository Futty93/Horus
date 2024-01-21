package main

import (
	"fmt"
	tea "github.com/charmbracelet/bubbletea"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

var ms = msg.GetGameMessageServerInstance()

type model struct {
	count        int
	serverStatus string
}

type serverStatusMsg struct {
	serverStatus string
}

func initialModel() model {
	return model{serverStatus: "NOT CREATED"}
}

func (model) Init() tea.Cmd {
	return nil
}

func (m model) View() string {
	// ui is message shown to user.
	var ui string
	ui = fmt.Sprintf("*** Simulator Console *** \n")
	ui += fmt.Sprintf("--- Server Status ---  \n")
	ui += m.serverStatus + "\n"
	return ui
}

func (m model) Update(tm tea.Msg) (tea.Model, tea.Cmd) {
	switch tm := tm.(type) {
	case tea.KeyMsg:
		switch tm.String() {
		case "r":
			return m, m.GetMessagePortNumber

		case "s":
			return m, m.StartGameMessageServer

		case "t":
			return m, m.StopMessageServer

		case "q", "ctl+c":
			return m, tea.Quit
		}
	case serverStatusMsg:
		m.serverStatus = tm.serverStatus
		return m, nil
	}

	return m, nil
}

func (m model) GetMessagePortNumber() tea.Msg {
	return serverStatusMsg{serverStatus: ms.GetStatusMessage()}
}

func (m model) StartGameMessageServer() tea.Msg {
	if !ms.IsActive() {
		ms.Start()
	}
	return serverStatusMsg{serverStatus: "Server is running."}
}

func (m model) StopMessageServer() tea.Msg {
	ms.Stop()
	return serverStatusMsg{serverStatus: "server stopped"}
}
