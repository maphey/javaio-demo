package org.maphey.study.jdk.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;

public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel channel;

	public ReadCompletionHandler(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		attachment.flip();
		byte[] body = new byte[attachment.remaining()];
		attachment.get(body);
		String req = new String(body);
		System.out.println("the time server receive order:" + req);
		String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req) ? LocalDateTime.now().toString() : "BAD ORDER";
		doWrite(currentTime);
	}

	private void doWrite(String currentTime) {
		if (currentTime != null && currentTime.length() > 0) {
			byte[] bytes = currentTime.getBytes();
			ByteBuffer writeBuff = ByteBuffer.allocate(bytes.length);
			writeBuff.put(bytes);
			writeBuff.flip();
			channel.write(writeBuff, writeBuff, new CompletionHandler<Integer, ByteBuffer>() {

				@Override
				public void completed(Integer result, ByteBuffer attachment) {
					if (attachment.hasRemaining()) {
						channel.write(attachment, attachment, this);
					}
				}

				@Override
				public void failed(Throwable exc, ByteBuffer attachment) {
					try {
						channel.close();
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
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
