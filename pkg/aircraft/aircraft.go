// Package aircraft provides types of aircraft for simulator.
package aircraft

type Aircraft interface {
	Proceed()
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
