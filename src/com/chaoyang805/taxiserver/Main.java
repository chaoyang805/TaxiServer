package com.chaoyang805.taxiserver;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class Main {

    public static void main(String[] args) {
        try {
            NioSocketAcceptor acceptor = new NioSocketAcceptor();

            acceptor.setHandler(new TaxiMessageHandler());

            acceptor.getFilterChain()
                    .addLast("codec",
                            new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
            acceptor.getSessionConfig().setBothIdleTime(60);
            acceptor.bind(new InetSocketAddress(9988));
            System.out.println("server started at localhost:9988");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
