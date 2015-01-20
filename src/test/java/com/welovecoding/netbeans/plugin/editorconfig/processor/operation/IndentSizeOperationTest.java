package com.welovecoding.netbeans.plugin.editorconfig.processor.operation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

public class IndentSizeOperationTest extends NbTestCase {

  private DataObject dataObject = null;
  private File file = null;

  public IndentSizeOperationTest(String testName) throws URISyntaxException, DataObjectNotFoundException {
    super(testName);

    String with4Spaces = "(function(){" + System.lineSeparator();
    with4Spaces += "    alert('Hello World!');" + System.lineSeparator();
    with4Spaces += "})();";

    try {
      file = File.createTempFile(this.getClass().getSimpleName(), ".js");
      Path path = Paths.get(Utilities.toURI(file));
      Files.write(path, with4Spaces.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
      dataObject = DataObject.find(FileUtil.toFileObject(file));
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  public void testSpaceConversion() throws Exception {
    String expected = "<html>"
            + "\n" + "  <b>Hello!</b>"
            + "</html>";

    Preferences codeStyle = CodeStylePreferences.get(
            dataObject.getPrimaryFile(),
            dataObject.getPrimaryFile().getMIMEType()
    ).getPreferences();

    // Check indent size before change (should be -1)
    int beforeChange = codeStyle.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
    assertEquals(-1, beforeChange);

    // Set indent size to 2
    codeStyle.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 2);

    // Save new style
    codeStyle.flush();

    // Check indent size after change
    int afterChange = codeStyle.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);

    assertEquals(2, afterChange);
  }
}
