package ie.gmit.ds;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ServerGrpc {
	private Server grpcServer;
	private static final Logger logger = Logger.getLogger(ServerGrpc.class.getName());
	private static final int PORT = 50550;
	private int portNum;

	private void start(int portNum) throws IOException, NoSuchAlgorithmException {
//	private void start() throws IOException, NoSuchAlgorithmException {
		grpcServer = ServerBuilder.forPort(portNum).addService(new PasswordServiceImpl()).build().start();
//		grpcServer = ServerBuilder.forPort(PORT).addService(new PasswordServiceImpl()).build().start();

		// System.out.println(portNumber);
		// setGrpcServer(portNumber);
		logger.info("Server started, listening on " + portNum);
	}

	@SuppressWarnings("unused")
	private void stop() {
		if (grpcServer != null) {
			grpcServer.shutdown();
		}
	}

	/*
	 * Await termination on the main thread since the gRPC library uses daemon
	 * threads
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (grpcServer != null) {
			grpcServer.awaitTermination();
		}
	}

	public void notifyServerPort() {
		System.out.println("User service connected to password service successfully");
	}

	public String serverMesage(String message) {
		return message;
	}

	/*
	 * Main method to run the server
	 * 
	 * For the "additional instructions for running the service (command-line
	 * parameters, etc.), I have included the ability to choose what port the server
	 * can run on. User will continuously be prompted to enter a port number. Port
	 * number must be within a specific range (50,000 - 59,999). The start method
	 * has been modified to allow for the port number to be passed in and the server
	 * will then be started on this port number
	 */
	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
		final ServerGrpc grpcServer = new ServerGrpc();

		int portNum = 0;
		
		Scanner scanner = new Scanner(System.in);
		boolean serverRunning = false;

		do {
			while (portNum < 50000 || portNum > 59999) {
				System.out.println("Enter port number that server can listen on (50000 - 59999): ");
				portNum = scanner.nextInt();

				if (portNum < 50000 || portNum > 59999) {
					System.out.println("ERROR: Invlaid port number entered. Please try again\n");
				} else {

					/*
					 * Try to start the server with the user-specified port number. If port number
					 * is already in use, notify user and prompt for another port number
					 */
					try {
						// grpcServer.start(portNum);
						grpcServer.start(portNum);

						grpcServer.serverMesage("Connection successful");

						grpcServer.blockUntilShutdown();

						serverRunning = true;
					} catch (Exception exception) {
						System.out.println("ERROR: Port not available. Please try again\n");
					}
				}
			}
		} while (serverRunning == false);

		// Close the scanner to prevent resource leak
		scanner.close();
	}
}