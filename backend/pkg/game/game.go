package game

import (
	"fmt"
	"sync"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/airspace"
)

var gameManagerSingletonOnce = sync.OnceValue(func() *Manager {
	return &Manager{
		snp: snapShot{as: *airspace.GetInstance()},
	}
})

func GetManagerInstance() *Manager {
	return gameManagerSingletonOnce()
}

type Game interface {
	Start()       // starts game
	Next()        // increase time by 1 sec
	LogSnapShot() // Output Log of SnapShot
}

type Manager struct {
	snp snapShot
	t   int
}

type snapShot struct {
	as airspace.Airspace
}

func (m *Manager) Start() {
	// reset time
	m.t = 0
	// create snapShot
	as := airspace.Airspace{}
	snp := snapShot{as: as}
	snp.as.SpawnAircraft()
}

func (m *Manager) Next() {
	// increase time
	m.t += 1
	// spawn a
	m.snp.as.SpawnAircraft()
}

func (m *Manager) LogSnapShot() {
	fmt.Println("***Game Logger Started***")
	fmt.Println(m.snp.as.String())
	fmt.Println("***Game Logger Ended***")
}
