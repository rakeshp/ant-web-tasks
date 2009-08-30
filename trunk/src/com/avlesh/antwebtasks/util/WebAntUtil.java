package com.avlesh.antwebtasks.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

public class WebAntUtil {
  private static final int BUF_SIZE = 8192;

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

  public static String readFully(Reader rdr) throws IOException {
    return readFully(rdr, BUF_SIZE);
  }

  public static String readFully(Reader rdr, int bufferSize) throws IOException {
      if (bufferSize <= 0) {
          throw new IllegalArgumentException("Buffer size must be greater "
                                             + "than 0");
      }
      final char[] buffer = new char[bufferSize];
      int bufferLength = 0;
      StringBuffer textBuffer = null;
      while (bufferLength != -1) {
          bufferLength = rdr.read(buffer);
          if (bufferLength > 0) {
              textBuffer = (textBuffer == null) ? new StringBuffer() : textBuffer;
              textBuffer.append(new String(buffer, 0, bufferLength));
          }
      }
      return (textBuffer == null) ? null : textBuffer.toString();
  }
}