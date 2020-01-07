package org.maphey.study.jdk.aio;

public class TimeServer {
	public static void main(String[] args) {
		int port = 8080;
		AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
		new Thread(timeServer, "aio-time server-1").start();
	}
}
