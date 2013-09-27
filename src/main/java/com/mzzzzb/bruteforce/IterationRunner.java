package com.mzzzzb.bruteforce;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mzzzzb.bruteforce.Status.Result;

public class IterationRunner implements Runnable {

	private Logger logger = LoggerFactory.getLogger(IterationRunner.class);
	private static String sevenZexe;
	private static String archiveLocation;

	public static String getSevenZexe() {
		return sevenZexe;
	}

	public static String getArchiveLocation() {
		return archiveLocation;
	}

	public static void setSevenZexe(String sevenZexe) {
		IterationRunner.sevenZexe = sevenZexe;
	}

	public static void setArchiveLocation(String archiveLocation) {
		IterationRunner.archiveLocation = archiveLocation;
	}

	private BlockingQueue<String> inputQueue;
	private BlockingQueue<Status> replyQueue;
	private boolean stop = false;

	public void stop() {
		stop = true;
	}

	public IterationRunner(BlockingQueue<String> inputQueue,
			BlockingQueue<Status> replyQueue) {
		super();
		this.inputQueue = inputQueue;
		this.replyQueue = replyQueue;
	}

	private String currentGuess;

	public String getCurrentGuess() {
		return currentGuess;
	}

	public void run() {
		Status reply = null;
		while (!stop || !inputQueue.isEmpty()) {
			try {
				currentGuess = inputQueue.take();
			} catch (InterruptedException e1) {
				logger.error("cannot take message from queue,", e1);
				continue;
			}
			try {
				reply = extractArchive(currentGuess);
			} catch (Exception e1) {
				logger.error("error extracting archive", e1);
				reply = new Status(currentGuess + ": " + e1.getMessage(),
						Result.ERROR);
			}
			try {
				replyQueue.put(reply);
			} catch (InterruptedException e) {
				logger.error("Could not send reply", e);
				if (reply.getResult() == Result.SUCCESS) {
					logger.error(
							"--------------FOUND PASSWORD!!!!!!!!!!!!!!!!!!! {}",
							reply.getGuess());
				}
			}
		}
	}

	private Status extractArchive(String generatedPassword)
			throws InterruptedException, IOException {

		String command = sevenZexe + " t " + archiveLocation;
		logger.debug("running {}", command);
		Process process = Runtime.getRuntime().exec(command);
		int resultstatus = -1;
		
		byte[] tosend = (generatedPassword + "\n").getBytes();
		process.getOutputStream().write(tosend);
		process.getOutputStream().flush();
		
		resultstatus = process.waitFor();

		Status status;
		switch (resultstatus) {
		case 0:
			status = new Status(generatedPassword, Result.SUCCESS);
			break;
		case 2: // FATAL error, most likely a pwd mismatch
			status = new Status(generatedPassword, Result.FAIL);
			break;
		default:
			// some other error
			status = new Status(generatedPassword + ":" + resultstatus,
					Result.ERROR);
		}

		return status;

		// InputStream stdout = process.getInputStream();
		// byte[] buffer = new byte[2048];
		// int bytesRead;
		// StringBuilder tmp = new StringBuilder();
		//
		// while ((bytesRead = stdout.read(buffer)) != -1) {
		//
		// for (int j = 0; j < bytesRead; j++) {
		// tmp.append((char) buffer[j]);
		// }
		//
		// }
		// if (tmp.toString().toLowerCase().contains("everything is ok")) {
		// foundPass = true;
		// }
	}

}
