package com.msftw.server;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
  private static final Logger log = LoggerFactory.getLogger(ServerMain.class);

  public static void main(String[] args) throws Exception {
    ServerMain.log.info("Starting jetty version {}", Server.getVersion());

    // configure jetty server
    Server jettyServer = new Server(8081);

    // setup up access log
    final NCSARequestLog requestLog = new NCSARequestLog("logs/access-yyyy_mm_dd.log");
    requestLog.setAppend(true);
    requestLog.setExtended(true);
    requestLog.setLogTimeZone("GMT");
    jettyServer.setRequestLog(requestLog);

    // setup web app
    final WebAppContext webapp = new WebAppContext();
    webapp.setResourceBase("src/main/webapp/");

    // pass webapp to jetty
    jettyServer.setHandler(webapp);

    // start web server
    jettyServer.start();
    jettyServer.join();
  }
}
