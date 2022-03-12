package com.yuganji.generator.output.sparrow;

import java.util.Map;

import com.yuganji.generator.exception.OutputHandleException;

import io.netty.channel.ChannelHandlerContext;

public interface ISocketServer {
    boolean startServer() throws OutputHandleException;
    boolean stopServer();
    boolean isActive();
    Map<String, ChannelHandlerContext> getClients();
    boolean stopClient(String id);
}
