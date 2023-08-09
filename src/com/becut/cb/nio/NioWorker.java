package com.becut.cb.nio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;

public class NioWorker<C extends AbstractSelectableChannel> {

    protected final C channel;
    protected final AbstractSelector selector;
    protected final SelectionKey key;
    protected final List<SelectionKey> selected;

    protected volatile Task impl;

    public NioWorker(C channel, int interestOps) throws IOException {
        this.channel = channel;
        selector = SelectorProvider.provider().openSelector();
        key = channel.register(selector, interestOps);
        selected = new ArrayList<>();
    }
    
    public void close() throws IOException {
        synchronized (selector) {
            key.cancel();
        }
        channel.close();
    }
    
    public void setImpl(Task impl) {
        this.impl = impl;
    }

    public void needNothing() {
        synchronized (selector) {
            key.interestOps(0);
        }
    }

    public void needConnect() {
        synchronized (selector) {
            key.interestOps(SelectionKey.OP_CONNECT);
        }
    }

    public void needAccept() {
        synchronized (selector) {
            key.interestOps(SelectionKey.OP_ACCEPT);
        }
    }

    public void needRead() {
        synchronized (selector) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public void needWrite() {
        synchronized (selector) {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    public void doWork() throws IOException {
        synchronized (selector) {
            if (selector.select(500) > 0) {
                selected.addAll(selector.selectedKeys());
                selector.selectedKeys().clear();
            }
        }
        for (SelectionKey k : selected) {
            if (k.isValid()) {
                int readyOps = k.readyOps();
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    onWrite();
                }
                if ((readyOps & SelectionKey.OP_READ) != 0) {
                    onRead();
                }
                if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
                    onAccept();
                }
                if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
                    onConnect();
                }
            }
        }
        selected.clear();
    }

    public static <C> C prepareSocket(AbstractSelectableChannel channel) throws IOException {
        channel.configureBlocking(false);
        if (channel instanceof SocketChannel) {
            Socket s = ((SocketChannel) channel).socket();
            try {
                s.setKeepAlive(true);
            } catch (SocketException ignore) {
            }
            try {
                s.setReuseAddress(true);
            } catch (SocketException ignore) {
            }
            try {
                s.setOOBInline(false);
            } catch (SocketException ignore) {
            }
            try {
                s.setTcpNoDelay(true);
            } catch (SocketException ignore) {
            }
        }
        return (C) channel;
    }

    protected void onConnect() throws IOException {
    }

    protected void onAccept() throws IOException {
    }

    protected void onRead() throws IOException {
    }

    protected void onWrite() throws IOException {
    }

}
