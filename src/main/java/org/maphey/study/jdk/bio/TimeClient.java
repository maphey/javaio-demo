package org.maphey.study.jdk.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeClient {
	public static void main(String[] args) {
		int port = 8080;
		Socket socket = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		try {
			socket = new Socket("127.0.0.1", port);
			InputStreamReader in = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(in);
			writer = new PrintWriter(socket.getOutputStream(), true);
			writer.println("query time order");
			System.out.println("send order to server succeed");
			String resp = reader.readLine();
			System.out.println(resp);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				writer.close();
			}
		}
	}
}
