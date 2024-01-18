package msg

import (
	"fmt"
	aircraftsPb "takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/pb/airplane"
)

func (s *server) GetAirplanesPositionStream(req *aircraftsPb.PositionRequest, stream aircraftsPb.Airplane_GetAirplanesPositionStreamServer) error {
	fmt.Println("Receive:Request for sending of airplanes position")
	for i := int32(0); i < 5; i++ {
		if err := stream.Send(&aircraftsPb.PositionReply{
			PosX:     0,
			PosY:     0,
			Heading:  0,
			Altitude: 1000,
		}); err != nil {
			return err
		}
	}
	return nil
}
