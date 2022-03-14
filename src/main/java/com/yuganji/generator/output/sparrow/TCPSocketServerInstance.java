package com.yuganji.generator.output.sparrow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yuganji.generator.ApplicationContextProvider;
import com.yuganji.generator.output.OutputService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
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
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TCPSocketServerInstance {
    private int port;
    private int id;
    private ChannelFuture cf;
    
    private EventLoopGroup parentGroup;
    private EventLoopGroup childGroup;
    private OutputService outputService;
    
    private int maxBuffer = 200;
    private volatile boolean state = true;
    @Getter
    private Map<String, ChannelHandlerContext> clients;
    
    private TCPSocketServerHandler handler;
    
    public TCPSocketServerInstance(int id, int port) {
        this.id = id;
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.startSender();
        this.outputService = ApplicationContextProvider.getApplicationContext().getBean(OutputService.class);
    }

    public void startSender() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (state) {
                    try {
                        if (clients.keySet().size() == 0) {
                            Thread.sleep(1_000);
                            continue;
                        }
                        List<Map<String, Object>> list = outputService.poll(id, maxBuffer);
                        if (list.size() == 0) {
                            Thread.sleep(1_000);
                            continue;
                        }
                        clients.entrySet().forEach(x -> {
                            x.getValue().writeAndFlush(list);
                        });

                        Thread.sleep(0, 10);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }).start();
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
                    handler = new TCPSocketServerHandler(clients);
                    p.addLast("encoder", new ObjectEncoder());
                    p.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                    p.addLast(handler);
                }
            });

            // 인커밍 커넥션을 액세스하기 위해 바인드하고 시작합니다.
            this.cf = sb.bind(this.port).sync();
            // 서버 소켓이 닫힐때까지 대기합니다.
            this.cf.channel().closeFuture().sync();
        }catch(Exception e){
            log.error(e.getMessage(), e);
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
        this.state = false;
        return true;
    }
    
    public boolean stopClient(String id) {
        if (!this.clients.containsKey(id)) {
            return false;
        }
        synchronized (this.clients) {
            this.clients.get(id).disconnect();
            this.clients.remove(id);
        }
        
        return true;
    }

    public boolean isRunning() {
        if (this.cf == null) {
            return false;
        }
        return cf.channel().isActive();
    }
}
