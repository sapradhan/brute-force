package com.mzzzzb.bruteforce;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	static Logger logger = LoggerFactory.getLogger(Main.class);
	BlockingQueue<String> processQ;
	BlockingQueue<Status> replyQ;

	GuessProvider guessProvider;
	List<IterationRunner> runners = new ArrayList<IterationRunner>();

	String start;
	String end;
	String correct;
	int threads;

	char[] charset;

	boolean stop;

	public void stop() {
		this.stop = true;
		for (IterationRunner r : runners) {
			r.stop();
		}
	}

	public Main() {
		init();
	}

	public static final String DEFAULT_CHARSET = "abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ1234567890";

	private final static String MAIN_CONFIG_FILE = "props.properties";
	private final static String RESUME_CONFIG_FILE = "resume.properties";

	private void loadProperties() {
		Properties prop = new Properties();
		InputStream m = null;
		try {
			m = new FileInputStream(MAIN_CONFIG_FILE);
			prop.load(m);
		} catch (IOException e1) {
			throw new RuntimeException("props.properties not found", e1);
		} finally {
			try {
				if (m != null)
					m.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m = null;
		}
		String chars = prop.getProperty("chars", DEFAULT_CHARSET);
		this.charset = chars.toCharArray();
		Arrays.sort(this.charset);
		
		String threadstr = prop.getProperty("threads", "9999");
		
		threads = Math.min(Integer.parseInt(threadstr), Runtime.getRuntime().availableProcessors() - 1);
		threads = threads < 1 ? 1 : threads;

		IterationRunner.setArchiveLocation(prop.getProperty("archive"));
		IterationRunner.setSevenZexe(prop.getProperty("7zexe", "7z"));

		int length = Integer.parseInt(prop.getProperty("length", "8"));

		char[] array = new char[length];
		Arrays.fill(array, charset[charset.length - 1]);
		this.end = new String(array);

		try {
			m = new FileInputStream(RESUME_CONFIG_FILE);
			prop.load(m);
			if (prop.containsKey("password")) {
				correct = prop.getProperty("password");
				logger.error("Password already fould: {}",
						correct);
				System.exit(0);
			}
			start = prop.getProperty("start", start);
			end = prop.getProperty("end", end);
			logger.info("Resuming from {} till {}", start, end);
		} catch (IOException e) {
			logger.info("resume.properties not found will start from beginning");
			start = "";
		} finally {
			try {
				if (m != null)
					m.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m = null;
		}
	}

	private void init() {

		loadProperties();

		validateProperties();

		guessProvider = new GuessProvider();
		guessProvider.setCharset(this.charset);
		guessProvider.setSubmitQ(processQ);


		processQ = new ArrayBlockingQueue<String>(threads + 1);
		replyQ = new ArrayBlockingQueue<Status>(2 * threads);

		for (int i = 0; i < threads; i++) {
			IterationRunner it = new IterationRunner(processQ, replyQ);
			runners.add(it);
		}

	}

	private void validateProperties() {
		assertThat(start.compareTo(end) < 0, "start is later than end :"
				+ start + ":" + end);
		for (char c : start.toCharArray()) {
			assertThat(Arrays.binarySearch(this.charset, c) >= 0,
					"Character in start not found in chars: " + c);
		}
		for (char c : end.toCharArray()) {
			assertThat(Arrays.binarySearch(this.charset, c) >= 0,
					"Character in end not found in chars: " + c);
		}

		File archive = new File(IterationRunner.getArchiveLocation());
		assertThat(archive.exists() && archive.isFile(), archive
				+ " does not exist ");

		// TODO check 7zexe is valid
		// TODO check archive is a valid archive

	}

	private void assertThat(boolean assertion, String message) {
		if (!assertion) {
			throw new RuntimeException(message);
		}
	}

	private ExecutorService iterationRunnerService;

	public void start() throws IOException, InterruptedException {
		iterationRunnerService = Executors.newFixedThreadPool(runners.size());

		for (IterationRunner runner : runners) {
			iterationRunnerService.submit(runner);
		}

		currentGuess = start;

		new Thread(new GuessGenerator()).start();

		PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(
				"output.txt", true)));

		try {
			while (!stop) {
				Status s = replyQ.take();
				switch (s.getResult()) {
				case SUCCESS:
					stop();
					correct = s.getGuess();
					logger.error("Password found {}", s.getGuess());
					output.println(String.format(" %d password found:  %s",
							System.currentTimeMillis(), s.getGuess()));
					replyQ.clear();
					System.exit(0);
					break;
				case FAIL:
					output.println(String.format(" %d trial failed:  %s",
							System.currentTimeMillis(), s.getGuess()));
					break;
				case ERROR:
					output.println(String.format(" %d error for :  %s",
							System.currentTimeMillis(), s.getGuess()));
				}
			}
		} finally {
			output.close();
		}

	}

	private String currentGuess;

	public class GuessGenerator implements Runnable {

		public void run() {
			while (!stop) {
				currentGuess = guessProvider.getNext(currentGuess);
				logger.debug("trying {}", currentGuess);
				try {
					processQ.put(currentGuess);
				} catch (InterruptedException e) {
					logger.error("error occured putting to processQ", e);
				}
			}

		}

	}

	public class ShutdownHook implements Runnable {

		public void run() {
			if (!stop) {
				logger.info("Stopping run, state will be saved to resume.properties");
				stop();
				SortedSet<String> notProcessed = new TreeSet<String>();
				processQ.drainTo(notProcessed);

				notProcessed.add(currentGuess);

				boolean gracefulexit = false;
				try {
					iterationRunnerService.shutdown();
					gracefulexit = iterationRunnerService.awaitTermination(20,
							TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.error("interrupted while saving", e);
				}
				if (!gracefulexit) {
					logger.warn(
							"timedout waiting for completion. you may want to replay last {} guesses",
							runners.size());
					List<Runnable> remainingjobsList = iterationRunnerService
							.shutdownNow();
					for (Runnable r : remainingjobsList) {
						IterationRunner runner = (IterationRunner) r;
						notProcessed.add(runner.getCurrentGuess());
					}
				}
				saveState(notProcessed.iterator().next());
			} else {
				saveState(null);
			}
		}

		private void saveState(String lowestNotProcessed) {
			PrintWriter resume = null;
			try {
				resume = new PrintWriter(new BufferedWriter(new FileWriter(
						RESUME_CONFIG_FILE)));
				if (correct == null) {
					resume.println(String
							.format("start=%s", lowestNotProcessed));
					resume.println(String.format("end=%s", end));
				} else {
					resume.println(String.format("password=%s", correct));
				}
			} catch (IOException e) {
				System.out.println("Could not write resume file");
				System.out.println("Resume info");
				if (correct == null) {
					System.out.println(String.format("start=%s",
							lowestNotProcessed));
					System.out.println(String.format("end=%s", end));
				} else {
					System.out.println(String.format("password=%s", correct));
				}
			} finally {
				if (resume != null) {
					resume.close();
				}
			}
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		Main m = new Main();
		Runtime.getRuntime().addShutdownHook(new Thread(m.new ShutdownHook()));
		m.start();

	}
}
