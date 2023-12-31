// Package aircraft provides types of aircraft for simulator.
package aircraft

type Aircraft struct {
	position   [2]float64
	heightFeet float64
}

func NewAircraft(position [2]float64, heightFeet float64) Aircraft {
	return Aircraft{
		position:   position,
		heightFeet: heightFeet,
	}
}
