package com.hklh8;

import com.hklh8.proxy.HttpProxyServer;

/**
 * Created by GouBo on 2018/1/14.
 */
public class Main {

    private static final int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort", "8080"));
    private static final String REMOTE_HOST = System.getProperty("remoteHost", "www.gov.cn");
    private static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "80"));
    private static final int PROXY_PORT = Integer.parseInt(System.getProperty("proxyPort", "8081"));

    public static void main(String[] args) throws Exception {
        HttpProxyServer server = new HttpProxyServer();
        server.start(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT, PROXY_PORT);
    }
}
