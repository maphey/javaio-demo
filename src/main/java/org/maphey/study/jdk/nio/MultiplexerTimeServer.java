package org.maphey.study.jdk.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private volatile boolean stop;

	public MultiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("the time server is start in port 8080");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			SelectionKey key = null;
			try {
				selector.select(1000);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					handleInput(key);
				}
			} catch (IOException e) {
				if (key != null) {
					key.cancel();
					if (key.channel() != null) {
						try {
							key.channel().close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				e.printStackTrace();
			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			if (key.isAcceptable()) {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			} else if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuff = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuff);
				if (readBytes > 0) {
					readBuff.flip();
					byte[] bytes = new byte[readBuff.remaining()];
					readBuff.get(bytes);
					String body = new String(bytes);
					System.out.println("the time server recerve order:" + body);
					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? LocalDateTime.now().toString()
							: "bad order";
					doWrite(sc, currentTime);
				} else if (readBytes < 0) {
					key.cancel();
					sc.close();
				}
			}
		}
	}

	private void doWrite(SocketChannel channel, String response) throws IOException {
		if (response != null && response.length() != 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuff = ByteBuffer.allocate(bytes.length);
			writeBuff.put(bytes);
			writeBuff.flip();
			channel.write(writeBuff);
		}
	}

}
