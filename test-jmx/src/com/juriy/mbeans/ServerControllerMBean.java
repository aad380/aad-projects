
/**
 * Copyright (c) 2013-2014 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 4 лют. 2014
 */

package com.juriy.mbeans;

public interface ServerControllerMBean{
  public int getCorePoolSize();
  public void setCorePoolSize(int corePoolSize);
  public int getMaxPoolSize();
  public void setMaxPoolSize(int maxPoolSize);
  public int getRejectedCount();
  public int getActiveThreads();
  public int getPassiveThreads();
  public int getTotalThreads();
  public void flushRejected();
}