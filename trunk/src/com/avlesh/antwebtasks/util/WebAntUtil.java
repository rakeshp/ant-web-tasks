package com.avlesh.antwebtasks.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class WebAntUtil {
  public static String getContentFromStream(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buffer = new byte[8 * 1024];
    int count = 0;
    do {
      bos.write(buffer, 0, count);
      count = in.read(buffer, 0, buffer.length);
    } while (count != -1);
    return bos.toString();
  }
}