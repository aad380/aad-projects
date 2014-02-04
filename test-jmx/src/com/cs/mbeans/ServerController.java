package com.cs.mbeans;

/**
 * Copyright (c) 2013-2014 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 4 лют. 2014
 */

import com.cs.server.*;

public class ServerController implements ServerControllerMBean {
  private TestServer server;
  public ServerController(TestServer server) {
    this.server = server;
  }

  public int getCorePoolSize() {
    return server.getExecutor().getCorePoolSize();
  }

  public void setCorePoolSize(int corePoolSize) {
    server.getExecutor().setCorePoolSize(corePoolSize);
  }

  public int getMaxPoolSize()   {
    return server.getExecutor().getMaximumPoolSize();
  }

  public void setMaxPoolSize(int maxPoolSize)   {
    server.getExecutor().setMaximumPoolSize(maxPoolSize);
  }

  public int getRejectedCount() {
    return server.getRejected();
  }

  public int getActiveThreads() {
    return server.getExecutor().getActiveCount();
  }

  public int getPassiveThreads() {
    return server.getExecutor().getPoolSize() - server.getExecutor().getActiveCount();
  }

  public int getTotalThreads() {
    return server.getExecutor().getPoolSize();
  }

  public void flushRejected() {
    server.flushRejected();
  }
}