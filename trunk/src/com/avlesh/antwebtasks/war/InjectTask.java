package com.avlesh.antwebtasks.war;

import com.avlesh.antwebtasks.util.WebAntUtil;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class InjectTask extends Task {
  protected Inject inject;
  public InjectTask() {
    super();
  }

  public void execute() throws BuildException {
    log("Hillo hai ji ..");
  }
}