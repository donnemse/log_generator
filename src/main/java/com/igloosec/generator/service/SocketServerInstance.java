package com.igloosec.generator.service;

import java.util.Map;
import java.util.Queue;

import com.igloosec.generator.engine.LogVO;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SocketServerInstance {
    private int port;
    private ChannelFuture cf;
    
    private EventLoopGroup parentGroup;
    private EventLoopGroup childGroup;
    private Queue<Map<String, Object>> queue;
    
    public SocketServerInstance(int port, Queue<Map<String, Object>> queue) {
        this.port = port;
        this.queue = queue;
    }

    public void start() {
        this.parentGroup = new NioEventLoopGroup(10);
        this.childGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(this.parentGroup, this.childGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 100)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel sc) throws Exception {
                    ChannelPipeline p = sc.pipeline();
//                    p.addLast("encoder", new ObjectEncoder());
//                    p.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                    p.addLast(new SocketServerHandler(queue));
                }
            });

            // 인커밍 커넥션을 액세스하기 위해 바인드하고 시작합니다.
            this.cf = sb.bind(this.port).sync();
            // 서버 소켓이 닫힐때까지 대기합니다.
            this.cf.channel().closeFuture().sync();
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }

    public boolean stop() {
        if (cf != null && cf.channel() != null && cf.channel().isActive()) {
            cf.channel().close();
        }
        if (this.parentGroup != null) {
            this.parentGroup.shutdownGracefully();
        }
        if (this.childGroup != null) {
            this.childGroup.shutdownGracefully();
        }
        
        return true;
    }

    public boolean isActive() {
        if (this.cf == null) {
            return false;
        }
        return cf.channel().isActive();
    }
}
