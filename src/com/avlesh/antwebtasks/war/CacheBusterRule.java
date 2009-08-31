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

import java.io.File;
import java.util.regex.Pattern;

public class CacheBusterRule {
  protected File file;
  protected Pattern from;
  protected String to;

  public boolean isCheckFileLastModifiedTime(){
    return file != null && file.exists();
  }

  public boolean isEmpty(){
    boolean isEmpty;
    if(!isCheckFileLastModifiedTime()){
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
}