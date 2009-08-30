package com.avlesh.antwebtasks.war;

import org.apache.tools.ant.BuildException;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class CacheBusterPreference implements Serializable {
  private String filePath;
  private long lastModifiedTime;
  private String version;

  public CacheBusterPreference(String filePath, long lastModifiedTime, String version) {
    this.filePath = filePath;
    this.lastModifiedTime = lastModifiedTime;
    this.version = version;
  }

  public static void savePreferencesToFile(Map<String, CacheBusterPreference> preferences, File file){
    try {
      FileOutputStream fout = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(preferences);
      oos.close();
      fout.close();
    }catch(Exception e) {
      throw new BuildException("Error serializing ..." + e.getMessage());
    }
  }

  public static Map<String, CacheBusterPreference> getPreferencesFromFile(File file){
    Map<String, CacheBusterPreference> preferences = new HashMap<String, CacheBusterPreference>();
    try {
      FileInputStream fin = new FileInputStream(file);
      ObjectInputStream ois = new ObjectInputStream(fin);
      preferences = (Map<String, CacheBusterPreference>) ois.readObject();
      ois.close();
      fin.close();
    }catch(Exception e){}
    return preferences;
  }

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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String toString() {
    return this.filePath + "=>" + new Date(this.lastModifiedTime).toString();
  }
}