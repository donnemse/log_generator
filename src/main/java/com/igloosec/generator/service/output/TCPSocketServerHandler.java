package com.igloosec.generator.service.output;

import java.util.Map;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Sharable
public class TCPSocketServerHandler extends ChannelInboundHandlerAdapter {
    
    private Map<String, ChannelHandlerContext> clients;
    

    public TCPSocketServerHandler(Map<String, ChannelHandlerContext> clients) {
        this.clients = clients;
    }
    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("channelRegistered");
        super.channelRegistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("active");
        
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String id = ((String) msg).replace("@dd_$3nd_", "");
        this.clients.put(id, ctx);
        log.debug("connected client: " + id);
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("channelReadComplete");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.debug("handlerAdded");
        super.handlerAdded(ctx);
    }
}
