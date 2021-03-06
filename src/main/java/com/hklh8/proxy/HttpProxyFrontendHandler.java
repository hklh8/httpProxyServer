package com.hklh8.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetSocketAddress;

public class HttpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;
    private final int proxyPort;

    private Channel outboundChannel;

    public HttpProxyFrontendHandler(String remoteHost, int remotePort, int proxyPort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.proxyPort = proxyPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop());
        b.channel(ctx.channel().getClass());
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpObjectAggregator(1024 * 1024 * 64),
                        new HttpProxyBackendHandler(inboundChannel)
                );
            }
        });
        b.option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(new InetSocketAddress(remoteHost, remotePort), new InetSocketAddress(proxyPort));
        outboundChannel = f.channel();
        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                // connection complete start to read first data
                inboundChannel.read();
            } else {
                // Close the connection if the connection attempt has failed.
                inboundChannel.close();
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            FullHttpRequest request = (FullHttpRequest) msg;
            request.headers().set(HttpHeaderNames.HOST, remoteHost);
            outboundChannel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
