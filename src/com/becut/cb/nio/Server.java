package com.becut.cb.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server extends NioWorker<ServerSocketChannel> {

    public Server(ServerSocketChannel channel) throws IOException {
        super(prepareSocket(channel), SelectionKey.OP_ACCEPT);
    }

    @Override
    protected void onAccept() {
        try {
            SocketChannel sc = channel.accept();
            Task task = impl;
            if (task == null) {
                needNothing();
                sc.close();
            } else {
                task.execute(sc);
            }

        } catch (IOException ex) {
        }
    }

}
