package com.avlesh.antwebtasks.war;

import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.regex.Pattern;

public class CacheBusterRule {
  protected File file;
  protected FileSet fileSet;
  protected Pattern from;
  protected String to;

  public void addFileset(FileSet fileSet){
    this.fileSet = fileSet;
  }

  public FileSet createFileset() {
    return new FileSet();
  }

  public boolean isEmpty(boolean checkLastModified){
    boolean isEmpty;
    if(!checkLastModified){
      isEmpty = (from == null || to == null);
    }else{
      isEmpty = (file == null || from == null || to == null);
    }
    return isEmpty;
  }

  public File getFile() {
    return file;
  }

  public Pattern getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public void setFrom(String from) {
    if(from != null && !"".equals(from.trim())){
      this.from = Pattern.compile(from);
    }
  }

  public FileSet getFileSet() {
    return fileSet;
  }

  public void setFileSet(FileSet fileSet) {
    this.fileSet = fileSet;
  }
}