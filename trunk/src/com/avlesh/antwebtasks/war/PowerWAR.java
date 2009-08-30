/**
 * Author: Avlesh Singh
 * avlesh@gmail.com
 * http://www.avlesh.com
 */

package com.avlesh.antwebtasks.war;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

public class PowerWAR extends War{
  private CacheBuster cacheBuster;
  private Inject inject;
  protected Map<Pattern, String> patternPropertyMap = new HashMap<Pattern, String>();
  protected Hashtable propertyMap;
  protected boolean applyCacheBusting = false;

  private Map<String, File> vPathInjectMap = new HashMap<String, File>();
  private Map<String, File> vPathCacheBustMap = new HashMap<String, File>();

  public PowerWAR(){
    super();
  }

  public void execute() throws BuildException {
    if(inject != null){
      if(inject.verbose){
        log("All build properties matching "+ inject.patternPrefix + "build.property.name" + inject.patternSuffix +
            " will be replaced with their corresponding values");
      }

      inject.setCaller(this);
      inject.init();
    }

    if(cacheBuster != null){
      if(cacheBuster.verbose){
        log("Preparing cache-busting rules.");
      }

      cacheBuster.setCaller(this);
      cacheBuster.init();
    }

    super.execute();
  }

  protected void zipFile(File file, ZipOutputStream zOut, String vPath, int mode) throws IOException {
    if(inject != null && inject.shouldInject(file)){
      vPathInjectMap.put(vPath, file);
      if(inject.modifyOriginal){
        file = inject.doInjection(file);
      }
    }

    if(cacheBuster != null && cacheBuster.shouldCacheBust(file)){
      vPathCacheBustMap.put(vPath, file);
      if(cacheBuster.modifyOriginal){
        file = cacheBuster.doCacheBusting(file);
      }
    }

    super.zipFile(file, zOut, vPath, mode);
  }

  protected void zipFile(InputStream in, ZipOutputStream zOut, String vPath,
                         long lastModified, File fromArchive, int mode) throws IOException {
    if(inject != null && vPathInjectMap.get(vPath) != null && !inject.modifyOriginal){
      in = inject.doInjection(in, vPathInjectMap.get(vPath).getPath());
    }

    if(cacheBuster != null && vPathCacheBustMap.get(vPath) != null && !cacheBuster.modifyOriginal){
      in = cacheBuster.doCacheBusting(in, vPathCacheBustMap.get(vPath).getPath());
    }

    super.zipFile(in, zOut, vPath, lastModified, fromArchive, mode);
  }

  protected void cleanUp() {
    if(cacheBuster != null && cacheBuster.isCheckFileLastModifiedTime()){
      Map<String, CacheBusterPreference> cacheBusterPreferences = new HashMap<String, CacheBusterPreference>();
      for(CacheBusterRule rule : cacheBuster.getRules()){
        if(rule.getFile() != null){
          String filePath = rule.getFile().getPath();
          CacheBusterPreference preference = cacheBuster.getCacheBusterPreferences().get(filePath);
          if(preference != null){
            if(rule.getFile().lastModified() > preference.getLastModifiedTime()){
              preference.setLastModifiedTime(rule.getFile().lastModified());
              preference.setVersion(cacheBuster.getVersionText());
            }
          }else{
            preference = new CacheBusterPreference(rule.getFile().getPath(),
                                                   rule.getFile().lastModified(),
                                                   cacheBuster.getVersionText());
          }
          cacheBusterPreferences.put(filePath, preference);
        }
      }
      CacheBusterPreference.savePreferencesToFile(cacheBusterPreferences, cacheBuster.getCacheBusterPreferencesFile());
    }
    super.cleanUp();
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
    if(cacheBuster == null){
      cacheBuster = new CacheBuster(this.getProject());
    }
    return cacheBuster;
  }
}