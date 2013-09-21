package com.mzzzzb.bruteforce;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class GuessProvider {
	BlockingQueue<String> submitQ;
	private char[] charset;

	public void setCharset(char[] charset) {
		this.charset = charset;
		Arrays.sort(this.charset);
	}
	

	public void setSubmitQ(BlockingQueue<String> submitQ) {
		this.submitQ = submitQ;
	}


	public String getNext(String guess) {

		if (getLastGuessOfLength(guess.length()).equals(guess)) {
			return getFirstGuessOfLength(guess.length() + 1);
		}

		return getNextGuessOfSameLength(guess);

	}

	private String getLastGuessOfLength(int length) {
		char[] array = new char[length];
		Arrays.fill(array, charset[charset.length - 1]);
		return new String(array);
	}

	private String getFirstGuessOfLength(int length) {
		char[] array = new char[length];
		Arrays.fill(array, charset[0]);
		return new String(array);
	}

	private String getNextGuessOfSameLength(String guess) {
		char[] guessArray = guess.toCharArray();
		int index = guessArray.length - 1;
		Character next;
		while ((next = nextChar(guessArray[index])) == null) {
			guessArray[index--] = charset[0];
		}
		guessArray[index] = next;
		return new String(guessArray);
	}

	private Character nextChar(char c) {
		if (charset[charset.length - 1] == c) {
			return null;
		} else {
			int index = Arrays.binarySearch(charset, 0, charset.length, c);
			if (index < 0)
				throw new NotInCharsetException(c, "");
			return charset[index + 1];
		}
	}

	public static class NotInCharsetException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		char c;
		String guess;

		public NotInCharsetException(char c, String guess) {
			super();
			this.c = c;
			this.guess = guess;
		}

	}

}
