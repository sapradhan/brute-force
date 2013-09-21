package com.mzzzzb.bruteforece;

import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.mzzzzb.bruteforce.GuessProvider;

public class GuessProviderTest {

	GuessProvider guessProvider;

	@Before
	public void setup() {
		guessProvider = new GuessProvider();
		guessProvider
				.setCharset("abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ1234567890"
						.toCharArray());
	}

	@Test
	public void testGetLastGuessOfLength() {
		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getLastGuessOfLength", 5), is("zzzzz"));
		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getLastGuessOfLength", 4), is("zzzz"));
		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getLastGuessOfLength", 0), is(""));
	}

	@Test
	public void testGetFirstGuessOfLength() {
		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getFirstGuessOfLength", 5), is("00000"));
	}

	@Test
	public void testGetNextGuessOfSameLength() {
		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getNextGuessOfSameLength", "00000"),
				is("00001"));

		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getNextGuessOfSameLength", "abcdef"),
				is("abcdeg"));

		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getNextGuessOfSameLength", "axcdez"),
				is("axcdf0"));

		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getNextGuessOfSameLength", "abczzz"),
				is("abd000"));

		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getNextGuessOfSameLength", "Pzzzzz"),
				is("Q00000"));
	}

	@Test
	public void testNextChar() {
		Assert.assertThat((Character) ReflectionTestUtils.invokeMethod(
				guessProvider, "nextChar", 'x'), is('y'));

		Assert.assertThat((Character) ReflectionTestUtils.invokeMethod(
				guessProvider, "nextChar", 'z'), CoreMatchers.nullValue());

	}

	@Test
	public void testGetNext() {
		Assert.assertThat((String) ReflectionTestUtils.invokeMethod(
				guessProvider, "getNext", ""), is("0"));
	}
}
