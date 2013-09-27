package com.mzzzzb.bruteforce;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

public class IterationRunnerTest {
	IterationRunner runner;

	@BeforeClass
	public static void beforeclass() throws IOException {
		Resource propR = new ClassPathResource("props.properties");
		Properties prop = new Properties();
		prop.load(propR.getInputStream());
		String sevenZexe = prop.getProperty("7zexe", "7z");
		IterationRunner.setSevenZexe(sevenZexe);
		Resource testArchive = new ClassPathResource("test-pwd.7z");
		IterationRunner.setArchiveLocation(testArchive.getFile()
				.getAbsolutePath());
	}

	@Before
	public void setup() {
		runner = new IterationRunner(new ArrayBlockingQueue<String>(5),
				new ArrayBlockingQueue<Status>(5));
	}

	@Test
	public void testExtractArchivePass() throws IOException {

		Status result = ReflectionTestUtils.invokeMethod(runner,
				"extractArchive", "pwd");

		Assert.assertEquals(Status.Result.SUCCESS, result.getResult());

	}

	@Test
	public void testExtractArchiveFail() throws IOException {

		Status result = ReflectionTestUtils.invokeMethod(runner,
				"extractArchive", "pwsd");

		Assert.assertEquals(Status.Result.FAIL, result.getResult());

	}

}
