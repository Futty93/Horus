package main

import (
	"github.com/charmbracelet/bubbles/list"
	tea "github.com/charmbracelet/bubbletea"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/msg"
)

var ms = msg.GetGameMessageServerInstance()

const (
	exit = iota
	mainMenu
	serverMenu
)

type model struct {
	mode         int        // mode of menu
	mainList     list.Model // List for main menu
	serverList   list.Model // List for Server menu
	serverStatus string
}

type serverStatusMsg struct {
	serverStatus string
}

type mainMenuItem struct {
	title, description string
	nextMode           int
}

func (i mainMenuItem) Description() string {
	return i.description
}
func (i mainMenuItem) Title() string {
	return i.title
}
func (i mainMenuItem) FilterValue() string {
	return i.title
}

var _ list.DefaultItem = (*mainMenuItem)(nil)
var mainMenuItems = []list.Item{
	mainMenuItem{title: "1.server menu", description: "grpc server menu", nextMode: serverMenu},
	mainMenuItem{title: "2.Exit", description: "Exit", nextMode: exit},
}

func initialModel() model {
	return model{
		mode:         mainMenu,
		mainList:     list.New(mainMenuItems, list.NewDefaultDelegate(), 1, 10),
		serverList:   list.New(serverMenuItems, list.NewDefaultDelegate(), 1, 20),
		serverStatus: "NOT CREATED"}
}

func (model) Init() tea.Cmd {
	return nil
}

func (m model) View() string {
	switch m.mode {
	case mainMenu:
		// Here is main menu view
		var mainList = m.mainList
		return mainList.View()
	case serverMenu:
		return m.ServerMenuView()
	default:
		return "mode is missing..."
	}
}

func (m model) Update(tm tea.Msg) (tea.Model, tea.Cmd) {
	switch m.mode {
	case serverMenu:
		return m.ServerMenuUpdate(tm)
	case mainMenu:
		var cmd tea.Cmd
		m.mainList, cmd = m.mainList.Update(tm)
		switch tm := tm.(type) {
		case tea.WindowSizeMsg:
			m.mainList.SetSize(tm.Width, tm.Height)
		case tea.KeyMsg:
			switch keypress := tm.String(); keypress {
			case "q", "ctl+c":
				return m, tea.Quit
			case "enter":
				i, ok := m.mainList.SelectedItem().(mainMenuItem)
				if ok {
					if i.nextMode == exit {
						return m, tea.Quit
					}
					m.mode = i.nextMode
				}
			}

		}
		return m, cmd

	default:
		return m, tea.Quit
	}

}
