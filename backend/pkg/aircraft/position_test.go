package aircraft

import (
	"math"
	"testing"
)

var EPSILON float64 = 0.001

func TestPosition_GoForward(t *testing.T) {
	p := position{location: [2]float64{0, 0}, heading: 360}
	p.GoForward(1)
	expected := [2]float64{0, 1}
	if difference(p.location, expected) > EPSILON {
		t.Errorf("expected: %f, get: %f", expected, p.location)
	}
}

func Test_headingToVec(t *testing.T) {
	heading := 360
	expected := [2]float64{0, 1}
	result := headingToVec(heading)
	if math.Abs(result[0]-expected[0]) > EPSILON {
		t.Errorf("expected: %f, get: %f", expected, result)
	}
	if math.Abs(result[1]-expected[1]) > EPSILON {
		t.Errorf("expected: %f, get: %f", expected, result)
	}
}
