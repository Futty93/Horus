package aircraft

import "fmt"

type PrintConsole interface {
	PrintPosition()
}

func (a Aircraft) PrintPosition() {
	fmt.Println("Aircraft position: ", a.position)
	fmt.Println("         height  : ", a.heightFeet)
}
