package ie.gmit.ds;

import java.util.logging.Logger;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

public class PasswordServiceImpl extends PasswordServiceGrpc.PasswordServiceImplBase {
	private static final Logger logger = Logger.getLogger(PasswordServiceImpl.class.getName());

	public PasswordServiceImpl() {

	}

	/**
	 * Override the hash function from PasswordServiceGrpc(non-Javadoc)
	 * 
	 * @see ie.gmit.ds.PasswordServiceGrpc.PasswordServiceImplBase#hash(ie.gmit.ds.
	 *      HashRequest, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void hash(HashRequest hashRequest, StreamObserver<HashResponse> streamObserver) {
		try {
			/**
			 * Need to convert to char array as the hash method in Passwords takes in a char
			 * array. Retrieve and store password from request from the client
			 */
			char[] password = hashRequest.getPassword().toCharArray();

			// Retrieves and stores the random salt to be used to hash a password
			byte[] passwordSalt = Passwords.getNextSalt();

			/*
			 * Hash the password, using the salt. The purpose of the salt (which is random)
			 * is to make sure that the hashes of passwords are never the same. If two users
			 * have the same password, the hashes for their password would be exactly the
			 * same if a salt wasn't used
			 */
			byte[] hashedPassword = Passwords.hash(password, passwordSalt);

			ByteString passwordByteString = ByteString.copyFrom(hashedPassword);
			ByteString saltByteString = ByteString.copyFrom(passwordSalt);

			streamObserver.onNext(HashResponse.newBuilder().setUserID(hashRequest.getUserID())
					.setHashedPassword(passwordByteString).setSalt(saltByteString).build());
			streamObserver.onCompleted();
		} catch (RuntimeException runtimeException) {
			/**
			 * From HashResponse.java
			 * 
			 * Get an instance of the type with no fields set. Because no fields are set,
			 * all getters for singular fields will return default values and repeated
			 * fields will appear empty
			 */
			streamObserver.onNext(HashResponse.newBuilder().getDefaultInstanceForType());
			streamObserver.onCompleted();
		}
		
		// streamObserver.onCompleted();
	}

	/**
	 * Override the validate function from PasswordServiceGrpc
	 * 
	 * @see ie.gmit.ds.PasswordServiceGrpc.PasswordServiceImplBase#validate(ie.gmit.ds.
	 *      ValidateRequest, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void validate(ValidateRequest validateRequest, StreamObserver<BoolValue> streamObserver) {
		try {
			ByteString getPasswordByteString = validateRequest.getHashedPassword();
			ByteString getSaltByteString = validateRequest.getSalt();

			char[] password = validateRequest.getPassword().toCharArray();
			byte[] salt = getSaltByteString.toByteArray();
			byte[] expectedHash = getPasswordByteString.toByteArray();

			if (Passwords.isExpectedPassword(password, salt, expectedHash) == true) {
				streamObserver.onNext(BoolValue.newBuilder().setValue(true).build());
				streamObserver.onCompleted();
			} else {
				streamObserver.onNext(BoolValue.newBuilder().setValue(false).build());
				streamObserver.onCompleted();
			}
		} catch (Exception exception) {
			// streamObserver.onNext(BoolValue.newBuilder().setValue(false).build());
			logger.info(exception.getLocalizedMessage());
		}
	}
}
