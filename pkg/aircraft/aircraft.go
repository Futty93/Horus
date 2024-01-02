// Package aircraft provides types of aircraft for simulator.
package aircraft

type Aircraft interface {
	Proceed()
}

type CommercialAircraft struct {
	pos         position
	instruction instruction
}

func (ca CommercialAircraft) Proceed(t int) {

}
