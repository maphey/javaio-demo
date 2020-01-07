package org.maphey.study.jdk.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeClientHandler implements Runnable, CompletionHandler<Void, AsyncTimeClientHandler> {
	private AsynchronousSocketChannel client;
	private String host;
	private int port;
	private CountDownLatch latch;

	public AsyncTimeClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			client = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		latch = new CountDownLatch(1);
		client.connect(new InetSocketAddress(host, port), this, this);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void completed(Void result, AsyncTimeClientHandler attachment) {
		byte[] req = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				if (attachment.hasRemaining()) {
					client.write(attachment, attachment, this);
				} else {
					ByteBuffer readBuff = ByteBuffer.allocate(1024);
					client.read(readBuff, readBuff, new CompletionHandler<Integer, ByteBuffer>() {

						@Override
						public void completed(Integer result, ByteBuffer attachment) {
							attachment.flip();
							byte[] bytes = new byte[attachment.remaining()];
							attachment.get(bytes);
							String body = new String(bytes);
							System.out.println("now is :" + body);
							latch.countDown();
						}

						@Override
						public void failed(Throwable exc, ByteBuffer attachment) {
							try {
								client.close();
								latch.countDown();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				latch.countDown();
			}
		});
	}

	@Override
	public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}

}
