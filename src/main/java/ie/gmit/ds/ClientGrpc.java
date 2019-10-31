package ie.gmit.ds;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class ClientGrpc {
	private static final Logger logger = Logger.getLogger(ClientGrpc.class.getName());
	private final ManagedChannel channel;
	private final PasswordServiceGrpc.PasswordServiceStub asyncPasswordService;
	private final PasswordServiceGrpc.PasswordServiceBlockingStub syncPasswordService;
	private ByteString passwordHashed;
	private ByteString salt;
	private String password;
	private int userID;
	private Scanner scanner = new Scanner(System.in);
	HashRequest hashRequest;
	HashResponse hashResponse;

	public ClientGrpc(String host, int port) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
		syncPasswordService = PasswordServiceGrpc.newBlockingStub(channel);
		asyncPasswordService = PasswordServiceGrpc.newStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public void hashRequest() {
		System.out.println("Enter user ID: ");
		userID = scanner.nextInt();

		System.out.println("Enter password: ");
		password = scanner.next();

		hashRequest = HashRequest.newBuilder().setUserID(userID).setPassword(password).build();
		hashResponse = HashResponse.newBuilder().getDefaultInstanceForType();

		try {
			// hashResponse = asyncPasswordService.hash(hashRequest, responseObserver);
			hashResponse = syncPasswordService.hash(hashRequest);

			passwordHashed = hashResponse.getHashedPassword();
			salt = hashResponse.getSalt();
		} catch (RuntimeException runtimeException) {
			logger.info(runtimeException.getLocalizedMessage());
			return;
		}
	}

	public void asyncPasswordValidation() {
		StreamObserver<BoolValue> responseObserver = new StreamObserver<BoolValue>() {
			@Override
			public void onNext(BoolValue boolValue) {
				if (boolValue.getValue()) {
					logger.info("Entered password is correct");
				} else {
					logger.info("Entered password is incorrect");
				}
			}

			@Override
			public void onError(Throwable throwable) {
				logger.info(throwable.getLocalizedMessage());
			}

			@Override
			public void onCompleted() {
				logger.info("Completed");
			}
		};

		try {
			asyncPasswordService.validate(ValidateRequest.newBuilder().setPassword(password).setSalt(salt)
					.setHashedPassword(passwordHashed).build(), responseObserver);

			TimeUnit.SECONDS.sleep(1);
		} catch (StatusRuntimeException | InterruptedException exception) {
			logger.info(exception.getLocalizedMessage());

			return;
		}

		return;
	}

	public static void main(String[] args) throws Exception {
		ClientGrpc clientGrpc = new ClientGrpc("localhost", 50551);
		Scanner scanner = new Scanner(System.in);
		int userID;
		String password;
		String repeatChoice;
		boolean keepAlive = true;

		while (keepAlive) {
			System.out.println("Enter user ID: ");
			userID = scanner.nextInt();

			System.out.println("Enter password: ");
			password = scanner.next();

			try {
				clientGrpc.hashRequest();
				clientGrpc.asyncPasswordValidation();

				System.out.println("Enter another user ID and password? (Y/N): ");
				repeatChoice = scanner.next();
				
				if (repeatChoice.equalsIgnoreCase("y")) {
					keepAlive = false;
				} else {
					keepAlive = true;
				}
			} finally {
				Thread.currentThread().join();
			}
		}
	}
}
