package com.avlesh.antwebtasks.war;

import java.io.Serializable;

public class CacheBusterPreferences implements Serializable {
  private String filePath;
  private long lastModifiedTime;

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public long getLastModifiedTime() {
    return lastModifiedTime;
  }

  public void setLastModifiedTime(long lastModifiedTime) {
    this.lastModifiedTime = lastModifiedTime;
  }
}