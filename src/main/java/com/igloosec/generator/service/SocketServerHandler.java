package com.igloosec.generator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SocketServerHandler extends ChannelInboundHandlerAdapter {
    
    private Queue<Map<String, Object>> queue;
    private int maxBuffer = 200;
    
    public SocketServerHandler(Queue<Map<String, Object>> queue) {
        this.queue = queue;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("active");
        ctx.writeAndFlush("asds");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
        
        while (true) {
            List<Map<String, Object>> list = new ArrayList<>();
            while(!queue.isEmpty() && list.size() < maxBuffer) {
                list.add(queue.poll());
                
            }
            System.out.println("poll");
            System.out.println(list);
            ctx.writeAndFlush(list);
            Thread.sleep(1_000);
        }
        
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.handlerAdded(ctx);
    }

    

  
   

}
