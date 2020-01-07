package org.maphey.study.netty.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServer {
	public static void main(String[] args) {
		new WebSocketServer().run(8080);
	}

	private void run(int port) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast("http-codec", new HttpServerCodec());
						pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
						ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
						pipeline.addLast("handler", new WebSockerServerHandler());
					}
				});
		try {
			Channel ch = b.bind(port).sync().channel();
			System.out.println("we socket server started at port " + port + ".");
			System.out.println("open your browser and navigate to http://localhost:" + port);
			ch.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
}
