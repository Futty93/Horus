package airspace

import (
	"sync"
	"takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/aircraft"
)

var airspaceSingletonOnce = sync.OnceValue(func() *Airspace {
	return &Airspace{}
})

func GetInstance() *Airspace {
	return airspaceSingletonOnce()
}

type Airspace struct {
	aircafts []aircraft.Aircraft
}

func (as *Airspace) SpawnAircraft() {
	as.aircafts = append(as.aircafts, aircraft.Aircraft(&aircraft.CommercialAircraft{}))
}

func (as *Airspace) String() string {
	var m string
	for _, a := range as.aircafts {
		m += a.String()
	}
	return m
}
