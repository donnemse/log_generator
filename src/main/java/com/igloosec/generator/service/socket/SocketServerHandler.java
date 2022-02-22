package com.igloosec.generator.service.socket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.igloosec.generator.queue.LogQueueService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SocketServerHandler extends ChannelInboundHandlerAdapter {
    
    private LogQueueService queueService;
    private int maxBuffer = 200;
    private int cnt = 0;
    private long lastCheckTime;
    private double eps = 0d;
    private Map<String, Channel> clients;
    private int port;
    
    public SocketServerHandler(int port, LogQueueService queueService) {
        this.port = port;
        this.queueService = queueService;
        this.clients = new HashMap<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("active");
//        ctx.channel()
        this.lastCheckTime = System.currentTimeMillis();
        
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ctx.ch
        this.clients.put(msg.toString(), ctx.channel());
        while (true) {
            if ((System.currentTimeMillis() - this.lastCheckTime) > 60 * 1000) {
                this.eps = this.cnt / ((System.currentTimeMillis() - this.lastCheckTime) / 1000.d);
                this.cnt = 0;
                this.lastCheckTime = System.currentTimeMillis();
            }
            
            List<Map<String, Object>> list = queueService.poll(this.port, this.maxBuffer);
            
            cnt += list.size();
            
            log.debug("poll");
            log.debug(list);
            ctx.writeAndFlush(list);
            Thread.sleep(1_000);
        }
        
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.handlerAdded(ctx);
    }

    public double getEPS() {
        // TODO Auto-generated method stub
        return eps;
    }
    
    public Map<String, Channel> getClients() {
        return this.clients;
    }
}
