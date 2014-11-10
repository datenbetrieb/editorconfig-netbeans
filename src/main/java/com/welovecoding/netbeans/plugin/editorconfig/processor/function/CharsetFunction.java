package com.welovecoding.netbeans.plugin.editorconfig.processor.function;

import com.welovecoding.netbeans.plugin.editorconfig.mapper.EditorConfigPropertyMapper;
import com.welovecoding.netbeans.plugin.editorconfig.model.FileAttributeName;
import com.welovecoding.netbeans.plugin.editorconfig.processor.ReadFileTask;
import com.welovecoding.netbeans.plugin.editorconfig.processor.Tab;
import com.welovecoding.netbeans.plugin.editorconfig.processor.WriteFileTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Michael Koppen
 */
public class CharsetFunction {

  private static final Logger LOG = Logger.getLogger(CharsetFunction.class.getName());

  public static boolean doCharset(DataObject dataObject, String ecCharset, final String lineEnding) {
    Charset requestedCharset = EditorConfigPropertyMapper.mapCharset(ecCharset);
    boolean wasChanged = false;

    LOG.log(Level.INFO, "{0}Set encoding to: \"{1}\".", new Object[]{Tab.TWO, requestedCharset.name()});

    FileObject fo = dataObject.getPrimaryFile();
    Charset currentCharset = getCharset(fo);

    if (currentCharset.name().equals(requestedCharset.name())) {
      LOG.log(Level.INFO, "{0}Action not needed: Encoding is already \"{1}\".",
              new Object[]{Tab.TWO, currentCharset.name()});
    } else {
      LOG.log(Level.INFO, "{0}Action: Rewriting file from encoding \"{1}\" to \"{2}\".",
              new Object[]{Tab.TWO, currentCharset.name(), requestedCharset.name()});

      final String content = new ReadFileTask(fo) {

        @Override
        public String apply(BufferedReader reader) {
          return reader.lines().collect(Collectors.joining(lineEnding));
        }
      }.call();

      boolean wasWritten = writeFile(new WriteFileTask(fo) {

        @Override
        public void apply(OutputStreamWriter writer) {
          try {
            writer.write(content);
          } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
          }
        }
      });

      if (wasWritten) {
        LOG.log(Level.INFO, "{0}Action: Successfully changed encoding to \"{1}\".", new Object[]{Tab.TWO, requestedCharset.name()});
        setFileAttribute(fo, FileAttributeName.ENCODING, requestedCharset.name());
        wasChanged = true;
      }
    }

    return wasChanged;
  }

  /**
   * TODO: It looks like "FileEncodingQuery.getEncoding" always returns "UTF-8".
   *
   * Even if the charset of that file is already UTF-16LE. Therefore we should
   * change our charset lookup. After the charset has been changed by us, we add
   * a file attribute which helps us to detect the charset in future.
   *
   * Maybe we should use a CharsetDetector:
   * http://userguide.icu-project.org/conversion/detection
   *
   * @param fo
   * @return
   */
  private static Charset getCharset(FileObject fo) {
    Object fileEncoding = fo.getAttribute(FileAttributeName.ENCODING);

    if (fileEncoding == null) {
      Charset currentCharset = FileEncodingQuery.getEncoding(fo);
      fileEncoding = currentCharset.name();
    }

    return Charset.forName((String) fileEncoding);
  }

  private static boolean writeFile(WriteFileTask task) {
    task.run();
    return true;
  }

  private static void setFileAttribute(FileObject fo, String key, String value) {
    try {
      fo.setAttribute(key, value);
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Error setting file attribute \"{0}\" with value \"{1}\" for {2}. {3}",
              new Object[]{
                key,
                value,
                fo.getPath(),
                ex.getMessage()
              });
    }
  }
}