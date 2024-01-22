package main

import (
	"fmt"
	"github.com/charmbracelet/bubbles/list"
	tea "github.com/charmbracelet/bubbletea"
)

const (
	backToMain = iota
	startServer
	stopServer
	serverStatus
)

type serverMenuItem struct {
	title, description string
	action             int
}

func (i serverMenuItem) Title() string {
	return i.title
}

func (i serverMenuItem) Description() string {
	return i.description
}
func (i serverMenuItem) FilterValue() string {
	return i.title
}

var _ list.DefaultItem = (*serverMenuItem)(nil)
var serverMenuItems = []list.Item{
	serverMenuItem{title: "Server Start", description: "grpc server will start.", action: startServer},
	serverMenuItem{title: "Stop Server", description: "grpc server will stop.", action: stopServer},
	serverMenuItem{title: "refresh", description: "refresh server status", action: serverStatus},
	serverMenuItem{title: "back to main", description: "<--", action: backToMain},
}

func (m model) ServerMenuView() string {
	// ui is message shown to user.
	var ui string
	ui += fmt.Sprintf("--- Server Status ---  \n")
	ui += m.serverStatus + "\n"
	ui += fmt.Sprintf("--- Server Menu ---\n")
	ui += m.serverList.View()
	return ui
}

func (m model) ServerMenuUpdate(tm tea.Msg) (tea.Model, tea.Cmd) {
	var cmd tea.Cmd
	m.serverList, cmd = m.serverList.Update(tm)
	switch tm := tm.(type) {
	case tea.WindowSizeMsg:
		m.serverList.SetSize(tm.Width, tm.Height)
	case tea.KeyMsg:
		switch tm.String() {
		case "ctl+c":
			return m, tea.Quit
		case "enter":
			i, ok := m.serverList.SelectedItem().(serverMenuItem)
			if ok {
				switch i.action {
				case backToMain:
					m.mode = mainMenu
				case startServer:
					return m, m.StartGameMessageServer
				case stopServer:
					return m, m.StopMessageServer
				case serverStatus:
					return m, m.GetMessagePortNumber
				default:
					panic("unhandled default case")
				}
			}
		}
		return m, cmd
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
