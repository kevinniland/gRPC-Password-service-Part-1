package ie.gmit.ds;

import java.io.IOException;

public class ServerRunner {
	public static void main(String[] args) throws IOException, InterruptedException {
		final GRPCServer grpcServer = new GRPCServer();
		grpcServer.start();
		grpcServer.blockUntilShutdown();
	}
}
