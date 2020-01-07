package org.maphey.study.jdk.aio;

public class TimeClient {
	public static void main(String[] args) {
		AsyncTimeClientHandler clientHandler = new AsyncTimeClientHandler("127.0.0.1", 8080);
		new Thread(clientHandler, "aio-asynctimeclienthandler-1").start();
	}
}
