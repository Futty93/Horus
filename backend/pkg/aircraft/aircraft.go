// Package aircraft provides types of aircraft for simulator.
package aircraft

import "fmt"

type Aircraft interface {
	Proceed(t float64)
	String() string
}

type CommercialAircraft struct {
	pos         position
	instruction instruction
	m           Movable
}

var _ Movable = (*position)(nil)

func (ca *CommercialAircraft) Proceed(t float64) {
	ca.m.GoForward(t)
}

func (ca *CommercialAircraft) NewTestCommercialAircraft() CommercialAircraft {
	a := CommercialAircraft{pos: position{heading: 360}}
	return a
}

func (ca *CommercialAircraft) String() string {
	return fmt.Sprintf("Aircraft Details: %s \n", ca.pos.String())
}
