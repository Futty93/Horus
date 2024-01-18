// Package aircraft provides types of aircraft for simulator.
package aircraft

import "fmt"

type Aircraft interface {
	Proceed(t int)
	String() string
}

type CommercialAircraft struct {
	pos         position
	instruction instruction
}

func (ca *CommercialAircraft) Proceed(t int) {

}

func (ca *CommercialAircraft) NewTestCommercialAircraft() CommercialAircraft {
	a := CommercialAircraft{pos: position{heading: 360}}
	return a
}

func (ca *CommercialAircraft) String() string {
	return fmt.Sprintf("Position: %s ", ca.pos.String())
}
