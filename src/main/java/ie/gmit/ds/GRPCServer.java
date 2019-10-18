package ie.gmit.ds;

import java.io.IOException;
import java.util.logging.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GRPCServer {
	private Server grpcServer;
	private static final Logger logger = Logger.getLogger(GRPCServer.class.getName());
	private static final int PORT = 50551;

	public void start() throws IOException {
		grpcServer = ServerBuilder.forPort(PORT).addService(new PasswordServiceImpl()).build().start();
		logger.info("Server started, listening on " + PORT);
	}

	@SuppressWarnings("unused")
	public void stop() {
		if (grpcServer != null) {
			grpcServer.shutdown();
		}
	}

	public void blockUntilShutdown() throws InterruptedException {
		if (grpcServer != null) {
			grpcServer.awaitTermination();
		}
	}
}