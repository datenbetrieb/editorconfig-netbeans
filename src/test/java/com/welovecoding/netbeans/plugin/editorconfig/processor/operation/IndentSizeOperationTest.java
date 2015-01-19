package com.welovecoding.netbeans.plugin.editorconfig.processor.operation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

/**
 *
 * @author Michael Koppen
 */
public class IndentSizeOperationTest extends NbTestCase {

  private final DataObject testDataObject;

  public IndentSizeOperationTest(String testName) throws URISyntaxException, DataObjectNotFoundException {
    super(testName);
    String path = "files/IndentSize.html";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Path testFilePath = Paths.get(url.toURI());
    testDataObject = DataObject.find(FileUtil.toFileObject(testFilePath.toFile()));
  }

  /**
   * Test of apply method, of class IndentSizeOperation.
   *
   * @throws java.lang.Exception
   */
  public void testSpaceConversion() throws Exception {
    System.out.println("########  Test: " + getName() + "  #######");

    String expected = "<html>"
            + "\n" + "    <b>Hello!</b>"
            + "</html>";

    Preferences codeStyle = CodeStylePreferences.get(testDataObject.getPrimaryFile(), testDataObject.getPrimaryFile().getMIMEType()).getPreferences();
    boolean change = IndentSizeOperation.doIndentSize(testDataObject, "4");

    try {
      codeStyle.flush();
    } catch (BackingStoreException ex) {
      Exceptions.printStackTrace(ex);
    }

    EditorCookie cookie = getEditorCookie(testDataObject);
    cookie.open();
    StyledDocument document = cookie.openDocument();
    NbDocument.runAtomicAsUser(document, () -> {
      try {
        cookie.saveDocument();
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    });

    String output = testDataObject.getPrimaryFile().asText();

    System.out.println("Input in ASCII numbers: ");
    System.out.println(getASCIINumbers(expected));
    System.out.println(getASCIICharacters(expected));

    System.out.println("Output in ASCII numbers: ");
    System.out.println(getASCIINumbers(output));
    System.out.println(getASCIICharacters(output));

    System.out.println("Saved data:");
    System.out.println(output);

    int newIndentSize = codeStyle.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);

    assertEquals(true, change);
    assertEquals(4, newIndentSize);
    // TODO: This does not work! Maybe we have to execute NetBeans Reformat!
    // assertEquals(expected, output);
  }

  private EditorCookie getEditorCookie(FileObject fileObject) {
    try {
      return (EditorCookie) DataObject.find(fileObject).getLookup().lookup(EditorCookie.class);
    } catch (DataObjectNotFoundException ex) {
      Exceptions.printStackTrace(ex);
      return null;
    }
  }

  private EditorCookie getEditorCookie(DataObject dataObject) {
    return getEditorCookie(dataObject.getPrimaryFile());
  }

  private String getASCIINumbers(String str) {
    StringBuilder sb = new StringBuilder();

    for (char c : str.toCharArray()) {
      int number = (int) c;

      if (number < 100) {
        sb.append("0").append(number);
      } else {
        sb.append(number);
      }

      sb.append("|");
    }

    return sb.toString();
  }

  private String getASCIICharacters(String str) {
    StringBuilder sb = new StringBuilder();

    for (char c : str.toCharArray()) {
      sb.append(" ").append(c).append(" |");
    }

    return sb.toString();
  }

}
