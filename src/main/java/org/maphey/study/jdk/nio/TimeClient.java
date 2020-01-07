package org.maphey.study.jdk.nio;

public class TimeClient {
	public static void main(String[] args) {
		TimeClientHandler handler = new TimeClientHandler("127.0.0.1", 8080);
		new Thread(handler, "time client-1").start();
	}
}
