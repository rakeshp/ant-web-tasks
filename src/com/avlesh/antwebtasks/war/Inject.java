/**
 * Copyright 2009 Avlesh Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avlesh.antwebtasks.war;

import com.avlesh.antwebtasks.util.WebAntUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inject {
  protected Task caller;
  protected List<FileSet> fileSets = new ArrayList<FileSet>();
  protected boolean verbose = true;
  protected String patternPrefix = "${";
  protected String patternSuffix = "}";
  protected Project project;
  protected Map<Pattern, String> patternPropertyMap = new HashMap<Pattern, String>();
  protected File injectionPropertyFile;
  protected boolean modifyOriginal = false;
  private List<String> filesToInject = new ArrayList<String>();

  public Inject(Project project){
    this.project = project;
  }

  public void addFileset(FileSet fileSet){
    this.fileSets.add(fileSet);
  }

  public FileSet createFileset() {
    return new FileSet();
  }

  public void init(){
    if(this.injectionPropertyFile != null){
      try{
        Properties properties = new Properties();
        properties.load(new FileInputStream(this.injectionPropertyFile));
        for(Object propertyName : properties.keySet()) {
          String propertyNameStr = (String)propertyName;
          patternPropertyMap.put(Pattern.compile(this.patternPrefix + propertyNameStr + this.patternPrefix, Pattern.LITERAL),
              properties.getProperty(propertyNameStr));
        }
      }catch(Exception ex){
        throw new BuildException(ex);
      }
    }else{
      Hashtable propertyMap = this.project.getProperties();
      for(Object propertyName : propertyMap.keySet()) {
        patternPropertyMap.put(Pattern.compile(this.patternPrefix + propertyName + this.patternPrefix, Pattern.LITERAL),
            (String)propertyMap.get(propertyName));
      }
    }

    for(FileSet fileSet : this.fileSets){
      File baseDirForThisFileSet = fileSet.getDir(this.project);
      String[] includedFilesInThisFileSet = fileSet.getDirectoryScanner(this.project).getIncludedFiles();
      for(String fileInThisFileSet : includedFilesInThisFileSet){
        this.filesToInject.add(new File(baseDirForThisFileSet, fileInThisFileSet).getPath());
      }
    }
  }

  public boolean shouldInject(File file){
    return this.filesToInject.isEmpty() || this.filesToInject.contains(file.getPath());
  }

  public InputStream doInjection(InputStream in, String filePath) throws IOException {
    InjectionResponse response = performInjection(in, filePath);
    return response.finalStream;
  }

  public File doInjection(File file) throws IOException {
    InputStream in = new FileInputStream(file);
    InjectionResponse response = performInjection(in, file.getPath());
    in.close();
    if(response.isModified){
      String modifiedFileContent = response.finalContent;
      FileWriter writer = new FileWriter(file);
      writer.write(modifiedFileContent);
      writer.close();
      file = new File(file.getPath());
    }
    return file;
  }

  private InjectionResponse performInjection(InputStream in, String filePath) throws IOException {
    boolean isModified = false;
    String fileContent = WebAntUtil.getContentFromStream(in);
    Set<Pattern> cachedPatterns = this.patternPropertyMap.keySet();
    for(Pattern pattern : cachedPatterns){
      Matcher matcher = pattern.matcher(fileContent);
      if(matcher.find()){
        if(this.verbose){
          caller.log("Replacing " + pattern.pattern() + " in " + filePath);
        }
        fileContent = fileContent.replaceAll(pattern.pattern(),
            this.patternPropertyMap.get(pattern));
        in = new ByteArrayInputStream(fileContent.getBytes());
        isModified = true;
      }
    }
    return new InjectionResponse(in, fileContent, isModified);
  }

  private class InjectionResponse{
    protected InputStream finalStream;
    protected boolean isModified;
    protected String finalContent;

    private InjectionResponse(InputStream finalStream, String finalContent, boolean modified) {
      this.finalStream = finalStream;
      this.isModified = modified;
      this.finalContent = finalContent;
    }
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public String getPatternPrefix() {
    return patternPrefix;
  }

  public void setPatternPrefix(String patternPrefix) {
    this.patternPrefix = patternPrefix;
  }

  public String getPatternSuffix() {
    return patternSuffix;
  }

  public void setPatternSuffix(String patternSuffix) {
    this.patternSuffix = patternSuffix;
  }

  public List<FileSet> getFileSets() {
    return fileSets;
  }

  public void setFileSets(List<FileSet> fileSets) {
    this.fileSets = fileSets;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public File getInjectionPropertyFile() {
    return injectionPropertyFile;
  }

  public void setInjectionPropertyFile(File injectionPropertyFile) {
    this.injectionPropertyFile = injectionPropertyFile;
  }

  public boolean isModifyOriginal() {
    return modifyOriginal;
  }

  public void setModifyOriginal(boolean modifyOriginal) {
    this.modifyOriginal = modifyOriginal;
  }

  public Task getCaller() {
    return caller;
  }

  public void setCaller(Task caller) {
    this.caller = caller;
  }

  public List<String> getFilesToInject() {
    return filesToInject;
  }

  public void setFilesToInject(List<String> filesToInject) {
    this.filesToInject = filesToInject;
  }
}