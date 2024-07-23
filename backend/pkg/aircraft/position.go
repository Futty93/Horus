package aircraft

import (
	"fmt"
	"math"
)

type Movable interface {
	GoForward(t float64)
}

// Base is the class for the stuff in the airspace.
// Parameters
type position struct {
	// 飛行機の位置
	location [2]float64
	// 飛行機の高度
	heightFeet float64
	// 飛行機の速度
	speedNt float64
	//飛行機の針路
	heading int
}

// GoForward changes airplane position
// 飛行機を前に進める関数
func (p *position) GoForward(t float64) {
	for i, _ := range p.location {
		p.location[i] += t * headingToVec(p.heading)[i]
	}
}

// headingToVec calculates airplane heading from degree to vector
func headingToVec(heading int) [2]float64 {
	yAxis, xAxis := math.Sincos((float64(heading) + 90) * math.Pi / 180)
	return [2]float64{xAxis, yAxis}
}

// difference calculates the length of 2 location
// result is just the number of difference, not distance like m or mile
func difference(p1 [2]float64, p2 [2]float64) float64 {
	return math.Sqrt(math.Pow(p1[0]-p2[0], 2) + math.Pow(p1[1]-p2[1], 2))
}

func (p *position) String() string {
	return fmt.Sprintf("Position: %v", p.location)
}
