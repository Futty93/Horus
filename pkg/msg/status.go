/*
 *
 * Copyright 2015 gRPC authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// Package msg implements a server for Greeter service.
package msg

import (
	"context"
	"flag"
	"fmt"
	"log"
	"net"

	"google.golang.org/grpc"
	pb "takahashi.qse.tohoku.ac.jp/atcGameProject/pb/status"
)

var (
	port = flag.Int("port", 50051, "The server port")
)

// server is used to implement status.GreeterServer.
type server struct {
	pb.UnimplementedServerStatusServer
}

// Status implements status.GreeterServer
func (s *server) Status(ctx context.Context, in *pb.StatusRequest) (*pb.StatusReply, error) {
	log.Printf("Received Status Check Request")
	return &pb.StatusReply{Message: "Hello" + in.GetName() + " :Server is running..."}, nil
}

func Start() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	pb.RegisterServerStatusServer(s, &server{})
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
