package org.maphey.study.netty.ws;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class WebSockerServerHandler extends SimpleChannelInboundHandler<Object> {
	private WebSocketServerHandshaker handshaker;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
		if (!msg.decoderResult().isSuccess() || !"websocket".equals(msg.headers().get("Upgrade"))) {
			sendHttpResponse(ctx, msg,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				"ws://localhost:8080/websocket", null, false);
		handshaker = wsFactory.newHandshaker(msg);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), msg);
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
		if (msg instanceof CloseWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(msg.content()));
			return;
		}
		if (!(msg instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(msg.getClass().getName() + " frame types not supported");
		}
		String request = ((TextWebSocketFrame) msg).text();
		System.out.println(ctx.channel() + " received " + request);
		ctx.channel().write(
				new TextWebSocketFrame(request + ",welcome use netty socket server, now:" + new Date().toString()));
	}

	private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest msg,
			DefaultFullHttpResponse defaultFullHttpResponse) {
		if (defaultFullHttpResponse.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(defaultFullHttpResponse.status().toString(), CharsetUtil.UTF_8);
			defaultFullHttpResponse.content().writableBytes();
			buf.release();
			HttpUtil.setContentLength(defaultFullHttpResponse, defaultFullHttpResponse.content().readableBytes());
		}
		ChannelFuture f = ctx.channel().writeAndFlush(defaultFullHttpResponse);
		if (!HttpUtil.isKeepAlive(msg) || defaultFullHttpResponse.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

}