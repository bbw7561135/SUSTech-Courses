import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays.*;

public class Tokenizer {
    String fileContent = new String("");
    int    pos = 0;

    public Tokenizer(String fileName)
        throws FileNotFoundException,
               UnsupportedEncodingException,
               IOException {
        // Constructor - reads and loads in memory.
        char[] cbuf = new char[200];
        int    charsRead;
        InputStreamReader isr = new InputStreamReader(new
                       FileInputStream(fileName), "UTF-8");
        while ((charsRead = isr.read(cbuf, 0, 200)) != -1) {
          fileContent += new String(java.util.Arrays.copyOfRange(cbuf,
                                    0, charsRead));
        }
        isr.close();
        // System.out.println(fileContent);
    }

    public String nextToken() {
        String  tok = "";
        char    c;
        boolean last_was_quote = false;

        try {
            while (! Character.isLetterOrDigit(fileContent
                                               .subSequence(pos, pos+1)
                                               .charAt(0))) {
              pos++;
            }
            c = fileContent.subSequence(pos,pos+1).charAt(0);
            while (Character.isLetterOrDigit(c)
                  || (last_was_quote = (c == '\''))) {
              tok += fileContent.substring(pos,pos+1);
              pos++;
              c = fileContent.subSequence(pos,pos+1).charAt(0);
            }
            if (last_was_quote) {
              // Remove ending quote
              while (tok.charAt(tok.length()-1) == '\'') {
                tok = tok.substring(0, tok.length()-1);
              }
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return tok.toLowerCase();
    }

}
