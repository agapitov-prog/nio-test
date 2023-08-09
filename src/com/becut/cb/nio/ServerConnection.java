package com.becut.cb.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerConnection extends NioWorker<SocketChannel> {

    public ServerConnection(SocketChannel channel) throws IOException {
        super(prepareSocket(channel), 0);
    }

    public void freeze() {
        needNothing();
    }
    
    public void cont() {
        needRead();
    }
    
    @Override
    protected void onRead() throws IOException {
        Task task = impl;
        if (task == null) {
            needNothing();
        } else {
            task.execute(channel);
        }
    }

}
