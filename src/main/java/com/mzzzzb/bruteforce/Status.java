package com.mzzzzb.bruteforce;

public class Status {
	enum Result {
		SUCCESS, FAIL, ERROR
	}

	private String guess;
	private Result result;

	public Status(String guess, Result result) {
		super();
		this.guess = guess;
		this.result = result;
	}

	public String getGuess() {
		return guess;
	}

	public Result getResult() {
		return result;
	}

}
