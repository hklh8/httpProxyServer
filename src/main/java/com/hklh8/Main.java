package com.hklh8;

import com.hklh8.proxy.HttpProxyServer;

/**
 * Created by GouBo on 2018/1/14.
 */
public class Main {

    static final int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort", "8080"));
    static final String REMOTE_HOST = System.getProperty("remoteHost", "www.gov.cn");
    static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "80"));

    public static void main(String[] args) throws Exception {
        HttpProxyServer server = new HttpProxyServer();
        server.start(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
    }
}
