package org.maphey.study.jdk.nio;

public class TimeServer {
	public static void main(String[] args) {
		int port = 8080;
		MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
		new Thread(timeServer, "nio-multiplexer time server-1").start();
	}
}
