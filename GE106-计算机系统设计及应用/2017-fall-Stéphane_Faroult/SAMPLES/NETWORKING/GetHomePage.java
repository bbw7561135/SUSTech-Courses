import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.*;

class TCPConnector {
    private String         hostName;
    private String         hostAddr;
    private int            port;
    private Socket         s = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    public TCPConnector(String host, int portNum) throws IOException {
        hostName = host;
        port = portNum;
        s = new Socket(host, portNum);
        // Set timeout to 1s
        s.setSoTimeout(1000);
        hostAddr = s.getInetAddress().toString().split("/")[1];
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    public void close() throws IOException {
      try {
        in.close();
        out.close();
        s.close();
      } catch (java.net.SocketException e) {
        // Do nothing
      }
    }

    public String getHostAddr() {
        return hostAddr;
    }

    public void send(String msg) throws IOException {
        out.write(msg);
        out.flush();  // IMPORTANT - it's buffered, this actually sends
                      // the message
    }

    public String receive() throws IOException {
        try { 
          String s = in.readLine();
          return s;
        } catch (SocketTimeoutException e) {
          return null;
        }
    }
}

class HTTPConnector extends TCPConnector {
    private StringBuffer header;
    private StringBuffer body;

    public HTTPConnector(String host) throws IOException {
        super(host, 80);
        // Allocate a new header and buffer
        header = new StringBuffer();
        body = new StringBuffer();
    }

    public String get(String pagename) throws IOException {
       String msg;
       // Clear previous header and body
       header.delete(0, header.length());
       body.delete(0, body.length());
       // Build a "get page" header
       header.append("GET " + pagename + " HTTP/1.1\n");
       header.append("Host: " + getHostAddr() + "\n");
       // The following line isn't officially mandatory
       // but some sites insist on it
       header.append("User-Agent: Java networking test program\n");
       // Add an empty line 
       header.append("\n");
       // Send the header
       send(header.toString());
       // Clear the header
       header.delete(0, header.length());
       // Get the answer to the request
       boolean reading_header = true;
       int     bytesToRead = -1;
       int     read = 0;
       // Important to test bytesToRead first.
       // Tests are performed in sequence, and
       // receive() will block until the connection
       // times out when the whole HTTP message has
       // been read.
       while ((bytesToRead != 0)
               && ((msg = receive()) != null)) {
          if (reading_header) {
            if (msg.trim().isEmpty()) {
               reading_header = false;
               // The header should be analyzed - it might
               // indicate an error!
            } else {
               // Minimal header analysis.
               // The various parameters could be stored,
               // for instance, in a HashMap.
               if (msg.toLowerCase().startsWith("content-length")) {
                  bytesToRead = Integer.parseInt(msg.split(":")[1].trim());
                  // System.out.println(Integer.toString(bytesToRead)
                  //                    + " bytes to read");
                  // System.out.flush();
               }
               header.append(msg);
            }
          } else {
            read = 1 + msg.length(); // 1 is added for the carriage return
                                     // character, stripped by readLine()
            body.append(msg);
            body.append("\n");       // Restore the end of line character
            bytesToRead -= read;
            // System.out.println(Integer.toString(bytesToRead)
            //                    + " bytes still to read");
            // System.out.flush();
          }
       }
       return body.toString();
    }
}

public class GetHomePage {

    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
          HTTPConnector h = new HTTPConnector(args[0]);
          System.out.println(h.get("/"));
          h.close();
        }
    }

}
