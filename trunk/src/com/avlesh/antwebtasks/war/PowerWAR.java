/**
 * Author: Avlesh Singh
 * avlesh@gmail.com
 * http://www.avlesh.com
 */

package com.avlesh.antwebtasks.war;

import com.avlesh.antwebtasks.web.CacheBuster;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class PowerWAR extends War{
  private CacheBuster cacheBuster;
  private Inject inject;
  protected Map<Pattern, String> patternPropertyMap = new HashMap<Pattern, String>();
  protected Hashtable propertyMap;
  protected boolean applyCacheBusting = false;
  protected static final String VERSION_FILE_TXT = "%\\{version-file-txt\\}";

  private Map<String, CacheBusterPreferences> cacheBusterPreferences = new HashMap<String, CacheBusterPreferences>();
  private Map<String, File> vPathMap = new HashMap<String, File>();

  public PowerWAR(){
    super();
  }

  public void addInject(Inject inject){
    this.inject = inject;
  }

  public Inject createInject() {
    if(inject == null){
      inject = new Inject(this.getProject());
    }
    return inject;
  }

  public void addCacheBuster(CacheBuster cacheBuster){
    this.cacheBuster = cacheBuster;
  }

  public CacheBuster createCacheBuster() {
    return new CacheBuster();
  }

  public void execute() throws BuildException {
    if(inject.verbose){
      log("All build properties matching "+ inject.patternPrefix + "build.property.name" + inject.patternSuffix +
          " will be replaced with their corresponding values");
    }

    inject.setCaller(this);
    inject.init();

//    if(cacheBuster != null){
//      if(cacheBuster.isEmpty() && cacheBuster.verbose()){
//        log("No rules are specified for the <cacheBuster>. You need to specify <rule> tags within the <cacheBuster> " +
//            "node, for which you want cache-busting to be applied");
//      }else if(cacheBuster.getVersionFile() == null){
//        log("\"versionFile\" is a required attribute in the <cacheBuster> tag. Your cache-busting rules cannot " +
//            "be applied without this file");
//      }else if(!cacheBuster.getVersionFile().exists()){
//        log("The version file \"" + cacheBuster.getVersionFile().getAbsolutePath() + "\" was not found. " +
//            "Please create this file and add your version data to the same.");
//      }else if(!cacheBuster.getVersionFile().canRead()){
//        log("The version file \"" + cacheBuster.getVersionFile().getAbsolutePath() + "\" is locked for reading. " +
//            "Cannot process your cache busting rules without reading this file.");
//      }else{
//        String versionText = getVersionText();
//        cacheBuster.setVersionText(versionText);
//        processRules();
//        if(cacheBuster.verbose()){
//          log("Version text: \"" + versionText + "\" will be used in your cache busting rules.");
//          log("Prepared all your cache-busting rules.");
//        }
//      }
//      applyCacheBusting = !cacheBuster.getRules().isEmpty();
//    }
    super.execute();
  }

  protected void cleanUp() {
    log("Executing clean-up");
    if(applyCacheBusting && cacheBuster.isCheckFileLastModifiedTime()){
      try{
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(cacheBuster.getCacheBusterPreferencesFile()));
        out.writeObject(cacheBusterPreferences);
        out.flush();
        out.close();
      }catch(Exception ex){
        log("Error saving your <cacheBuster> preferences");
      }
    }
    super.cleanUp();
  }

  protected void zipFile(File file, ZipOutputStream zOut, String vPath, int mode) throws IOException {
    if(inject.shouldInject(file)){
      vPathMap.put(vPath, file);
      if(inject.modifyOriginal){
        file = inject.doInjection(file);
      }
    }

    boolean fileModified = false;
    if(
      applyCacheBusting &&
      cacheBuster.isModifyOriginalFile() &&
      cacheBuster.shouldDoCacheBustingForFile(vPath) &&
      ((cacheBuster.isCheckFileLastModifiedTime() && cacheBusterPreferences.get(vPath).getLastModifiedTime() > file.lastModified())
       || !cacheBuster.isCheckFileLastModifiedTime())
    ){
      log("Modifying the file for cache-busting: " + file.getPath());
      boolean cacheBustingMatchesFound = cacheBuster.doCacheBusting(file, vPath, this);
      if(cacheBustingMatchesFound){
       fileModified = true;
      }
    }

    if(fileModified){
      //cacheBusterPreferences.get(file.getPath()).setLastModifiedTime(file.lastModified());
    }
    super.zipFile(file, zOut, vPath, mode);
  }

  protected void zipFile(InputStream in, ZipOutputStream zOut, String vPath,
                         long lastModified, File fromArchive, int mode) throws IOException {
    if(vPathMap.get(vPath) != null && !inject.modifyOriginal){
      in = inject.doInjection(in, new File(vPathMap.get(vPath), vPath).getPath());
    }

//    if(
//      applyCacheBusting &&
//      !cacheBuster.isModifyOriginalFile() &&
//      cacheBuster.shouldDoCacheBustingForFile(vPath) &&
//      ((cacheBuster.isCheckFileLastModifiedTime() &&
//          cacheBusterPreferences.get(file.getPath()).getLastModifiedTime() > lastModified)
//       || !cacheBuster.isCheckFileLastModifiedTime())
//    ){
//      log("Modifying the input-stream for cache-busting: " + file.getPath());
//      in = cacheBuster.doCacheBusting(in, vPath, this);
//    }
//
    super.zipFile(in, zOut, vPath, lastModified, fromArchive, mode);
  }

  protected String getVersionText(){
    String versionData;
    if(cacheBuster.getVersionPropertyKey() != null){
      Properties properties = new Properties();
      try{
        properties.load(new FileInputStream(cacheBuster.getVersionFile()));
      }catch(Exception ex){
        throw new BuildException("Error reading your version property file: " + cacheBuster.getVersionFile().getAbsolutePath());
      }
      versionData = properties.getProperty(cacheBuster.getVersionPropertyKey());
      if(versionData == null || "".equals(versionData.trim())){
        throw new BuildException("Your version file: " + cacheBuster.getVersionFile().getAbsolutePath() + " does not " +
            "contain any version data against the property: " + cacheBuster.getVersionPropertyKey() + ". " +
            "Please check your version file. Skipping all your cache busting rules ...");
      }
    }else{
      try{
        versionData = FileUtils.readFully(new FileReader(cacheBuster.getVersionFile()));
      }catch(IOException ex){
        throw new BuildException("Error reading your version file: " + cacheBuster.getVersionFile().getAbsolutePath());
      }

      if(versionData == null || "".equals(versionData.trim())){
        throw new BuildException("Your version file: " + cacheBuster.getVersionFile().getAbsolutePath() + " does not " +
            "contain any version data. Please check your version file. Skipping all your cache busting rules ...");
      }
    }
    versionData = versionData.trim();
    return versionData;
  }

  protected final void processRules(){
    File cacheBusterPreferencesFile = cacheBuster.getCacheBusterPreferencesFile();
    if(cacheBuster.isCheckFileLastModifiedTime() || cacheBuster.isModifyOriginalFile()){
      try{
        if(!cacheBusterPreferencesFile.exists()){
          cacheBusterPreferencesFile.createNewFile();
        }else{
          FileInputStream fin = new FileInputStream(cacheBusterPreferencesFile);
          ObjectInputStream ois = new ObjectInputStream(fin);
          cacheBusterPreferences = (Map<String, CacheBusterPreferences>) ois.readObject();
          ois.close();
        }
      }catch(Exception ex){
        log("Error in reading values from your <cacheBuster> preferences file.");
      }
    }

    List<CacheBuster.Rule> rules = cacheBuster.getRules();
    for(CacheBuster.Rule rule : rules){
      if(rule.isEmpty(cacheBuster)){
        if(cacheBuster.isCheckFileLastModifiedTime()){
          throw new BuildException("A <rule> tag should have a valid \"file\" attribute, " +
              "if \"checkFileLastModifiedTime\" is set to true in the <cacheBuster>.");
        }else{
          throw new BuildException("A <rule> tag, inside cache-buster, without valid \"file\" or " +
              "\"urlPattern\" or \"replaceFormat\" attributes is considered invalid.");
        }
      }

      if(cacheBuster.isCheckFileLastModifiedTime() || cacheBuster.isModifyOriginalFile()){
        File ruleFile = rule.getFile();
        String filePath = ruleFile.getPath();
        if(ruleFile.exists()){
          if(!cacheBusterPreferences.containsKey(filePath)){
            CacheBusterPreferences preferences = new CacheBusterPreferences();
            preferences.setFilePath(filePath);
            preferences.setLastModifiedTime(ruleFile.lastModified());
            cacheBusterPreferences.put(filePath, preferences);
          }else{
            //update the last modified time.
            cacheBusterPreferences.get(filePath).setLastModifiedTime(ruleFile.lastModified());
          }
        }else{
          throw new BuildException("The file: " + filePath + " does not exist.");
        }
      }

      String replaceFormat = rule.getReplaceFormat();
      if(replaceFormat.indexOf(VERSION_FILE_TXT) >= 0){
        replaceFormat = replaceFormat.replaceAll(VERSION_FILE_TXT, cacheBuster.getVersionText());
        rule.setReplaceFormat(replaceFormat);
      }
    }
  }
}