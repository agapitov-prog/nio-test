package com.becut.cb.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientConnection extends NioWorker<SocketChannel> {

    public ClientConnection(SocketChannel channel) throws IOException {
        super(prepareSocket(channel), SelectionKey.OP_CONNECT);
    }

    public void connect(SocketAddress address) throws IOException {
        if (channel.connect(address)) {
            needNothing();
        }
    }

    public boolean isConnected() {
        return channel.isConnected();
    }

    public void freeze() {
        needNothing();
    }

    public void cont() {
        needWrite();
    }

    @Override
    protected void onConnect() throws IOException {
        if (channel.finishConnect()) {
            needNothing();
        }
    }

    @Override
    protected void onWrite() throws IOException {
        Task task = impl;
        if (task == null) {
            needNothing();
        } else {
            task.execute(channel);
        }
    }

}
