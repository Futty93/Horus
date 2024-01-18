package msg

import (
	"context"
	aircraftsPb "takahashi.qse.tohoku.ac.jp/atcGameProject/pb/airplane"
)

func (s *server) Location(ctx context.Context, in *aircraftsPb.LocationRequest) (*aircraftsPb.LocationReply, error) {
	return &aircraftsPb.LocationReply{PosX: 0, PosY: 0, Heading: 0, Altitude: 0}, nil
}
