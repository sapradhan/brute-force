package com.mzzzzb.bruteforce;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.junit.Test;

public class MainTest {

//	@Test
	public void test() throws IOException, InterruptedException {
		// Main.main(new String[] {});
		ReadableByteChannel c = Channels.newChannel(System.in);
		ByteBuffer b = ByteBuffer.allocate(8);
		boolean stop = false;
		while (!stop) {
			int a = c.read(b);
			if (a > 0) {
				b.flip();
				char cc = b.getChar();
				if (cc == 'x') {
					stop = true;
				}
				b.clear();
			}
			System.out.println("waiting");
			Thread.sleep(1000);
		}
		String command = "7z t -ppwd  /home/mzzzzb/test7z/valid-pwd.7z";
		Process process = Runtime.getRuntime().exec(command);
		int resultstatus = -1;
		resultstatus = process.waitFor();
		System.out.println(resultstatus);
	}

}
