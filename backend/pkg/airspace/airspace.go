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
	aircrafts []aircraft.Aircraft
}

func (as *Airspace) SpawnAircraft() {
	as.aircrafts = append(as.aircrafts, aircraft.Aircraft(&aircraft.CommercialAircraft{}))
}

func (as *Airspace) String() string {
	var m string
	for _, a := range as.aircrafts {
		m += a.String()
	}
	return m
}
