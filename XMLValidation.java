import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class XMLValidation {
  static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
  final static File xsdFile = new File("InfoModelNetbeheer.xsd");

  public static void main(String[] args) {
    Validator validator;
    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory.newSchema(xsdFile);
      validator = schema.newValidator();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.exit(validate(validator));
  }

  public static int validate(Validator validator) {
    var cnt = validatePass(validator);
    cnt += validateFail(validator);
    return cnt;
  }

  public static int validatePass(Validator validator) {
    var cnt = 0;
    var dir = Path.of("test/pass").toAbsolutePath().toFile();
    for (File f : dir.listFiles((d, name) -> name.endsWith(".xml"))) {
      LOGGER.info(String.format("Validating %s", Path.of("test/pass", f.getName())));
      var result = validateXMLSchema(validator, f);
      if (result != null) {
        cnt++;
        LOGGER.severe(String.format("Exception while parsing %s: (%s)", f.getName(), result));
      }
    }
    return cnt;
  }

  public static int validateFail(Validator validator) {
    var cnt = 0;
    var dir = Path.of("test/fail").toAbsolutePath().toFile();
    for (File f : dir.listFiles((d, name) -> name.endsWith(".xml"))) {
      LOGGER.info(String.format("Validating %s", Path.of("test/fail", f.getName())));
      var result = validateXMLSchema(validator, f);
      if (result == null) {
        cnt++;
        LOGGER.severe(String.format("Validation passed unexpectedly: %s", f.getName()));
      }
    }
    return cnt;
  }

  public static String validateXMLSchema(Validator validator, File xmlFile) {
    try {
      validator.validate(new StreamSource(xmlFile));
    } catch (Exception e) {
      return e.getMessage();
    }
    return null;
  }
}
