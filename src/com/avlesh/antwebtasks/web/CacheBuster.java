package com.avlesh.antwebtasks.web;

import org.apache.tools.ant.util.FileUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import com.avlesh.antwebtasks.util.WebAntUtil;
import com.avlesh.antwebtasks.war.PowerWAR;

public class CacheBuster {
  private File versionFile;
  private String versionPropertyKey;
  private String versionText;
  private List<Pattern> includes = new ArrayList<Pattern>();
  private boolean verbose = true;
  private boolean modifyOriginalFile = false;
  private boolean checkFileLastModifiedTime = false;
  private File cacheBusterPreferencesFile = new File(".cache-buster.pref");
  private List<Rule> rules = new ArrayList<Rule>();

  public InputStream doCacheBusting(InputStream in, String vPath, PowerWAR task) throws IOException{
    String fileContent = WebAntUtil.getContentFromStream(in);
    if(fileContent != null) {
      fileContent = performReplace(vPath, task, fileContent);
      in = new ByteArrayInputStream(fileContent.getBytes());
    }
    return in;
  }

  public boolean doCacheBusting(File file, String vPath, PowerWAR task) throws IOException {
    boolean fileModified = false;
    String fileContent = FileUtils.readFully(new FileReader(file));
    if(fileContent != null) {
      String replacedFileContent = performReplace(vPath, task, fileContent);
      //if a replace happend, override the file contents with the new data
      if(!fileContent.equals(replacedFileContent)) {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(replacedFileContent);
        fileWriter.flush();
        fileWriter.close();
        fileModified = true;
      }
    }
    return fileModified;
  }

  private String performReplace(String vPath, PowerWAR task, String fileContent) {
    for(Rule rule : rules) {
      Matcher urlPatternMatcher = rule.urlPattern.matcher(fileContent);
      //if a url pattern is found in the file, proceed further
      if(urlPatternMatcher.find()) {
        task.log("Match found in " + vPath + "; replacing all: " + rule.urlPattern.pattern());
        fileContent = urlPatternMatcher.replaceAll(rule.replaceFormat);
      }
    }
    return fileContent;
  }

  public boolean shouldDoCacheBustingForFile(String vPath){
    boolean matchesAnIncludePattern = includes.isEmpty();
    if(!includes.isEmpty()){
      for(Pattern includePattern : includes){
        Matcher matcher = includePattern.matcher(vPath);
        if(matcher.find()){
          matchesAnIncludePattern = true;
          break;
        }
      }
    }
    return matchesAnIncludePattern;
  }

  public boolean isEmpty(){
    return rules.isEmpty();
  }

  public Rule createRule() {
    return new Rule();
  }

  public void addRule(Rule rule){
    this.rules.add(rule);
  }

  public static class Rule {
    protected File file;
    protected Pattern urlPattern;
    protected String replaceFormat;
    protected String initialUrlPattern;

    public boolean isEmpty(CacheBuster cacheBuster){
      boolean isEmpty;
      if(!cacheBuster.isCheckFileLastModifiedTime()){
        isEmpty = (urlPattern == null || replaceFormat == null);
      }else{
        isEmpty = (file == null || urlPattern == null || replaceFormat == null);
      }
      return isEmpty;
    }

    public File getFile() {
      return file;
    }

    public Pattern getUrlPattern() {
      return urlPattern;
    }

    public String getReplaceFormat() {
      return replaceFormat;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public void setReplaceFormat(String replaceFormat) {
      this.replaceFormat = replaceFormat;  
    }

    public void setUrlPattern(String urlPattern) {
      initialUrlPattern = urlPattern;
      if(urlPattern != null && !"".equals(urlPattern.trim())){
        this.urlPattern = Pattern.compile(urlPattern);
      }
    }

    public String getInitialUrlPattern() {
      return initialUrlPattern;
    }

    public void setInitialUrlPattern(String initialUrlPattern) {
      this.initialUrlPattern = initialUrlPattern;
    }
  }

  public File getVersionFile() {
    return versionFile;
  }

  public void setVersionFile(File versionFile) {
    this.versionFile = versionFile;
  }

  public List<Pattern> getIncludes() {
    return includes;
  }

  public void setIncludes(String includes) {
    if(includes != null){
      String[] includedFiles = includes.split("\\s*,\\s*");
      for(String includedFile : includedFiles){
        this.includes.add(Pattern.compile(includedFile));
      }
    }
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public List<Rule> getRules() {
    return rules;
  }

  public void setRules(List<Rule> rules) {
    this.rules = rules;
  }

  public String getVersionPropertyKey() {
    return versionPropertyKey;
  }

  public void setVersionPropertyKey(String versionPropertyKey) {
    this.versionPropertyKey = versionPropertyKey;
  }

  public String getVersionText() {
    return versionText;
  }

  public void setVersionText(String versionText) {
    this.versionText = versionText;
  }

  public boolean isModifyOriginalFile() {
    return modifyOriginalFile;
  }

  public void setModifyOriginalFile(boolean modifyOriginalFile) {
    this.modifyOriginalFile = modifyOriginalFile;
  }

  public boolean isCheckFileLastModifiedTime() {
    return checkFileLastModifiedTime;
  }

  public void setCheckFileLastModifiedTime(boolean checkFileLastModifiedTime) {
    this.checkFileLastModifiedTime = checkFileLastModifiedTime;
  }

  public File getCacheBusterPreferencesFile() {
    return cacheBusterPreferencesFile;
  }

  public void setCacheBusterPreferencesFile(File cacheBusterPreferencesFile) {
    this.cacheBusterPreferencesFile = cacheBusterPreferencesFile;
  }
}