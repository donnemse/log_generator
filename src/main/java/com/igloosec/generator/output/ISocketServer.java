package com.igloosec.generator.output;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public interface ISocketServer {
    void startServer();
    boolean stopServer();
    boolean isActive();
    Map<String, ChannelHandlerContext> getClients();
    boolean stopClient(String id);
}
