import java.io.*;
import java.net.*;

public class FilmClient {

    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java FilmClient <host name> <port number>");
            System.exit(1);
        }

        String  hostName = args[0];
        int     portNumber = Integer.parseInt(args[1]);
        boolean loop = true;

        while (loop) {
          try (
            Socket         sock = new Socket(hostName, portNumber);
            PrintWriter    out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(sock.getInputStream()));
          ) {
            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            System.out.print("Query> ");
            // read from input
            fromUser = stdIn.readLine();
            // send to server
            out.println(fromUser);
            // read from server
            while ((fromServer = in.readLine()) != null) {
              if (fromServer.length() > 0) {
                System.out.println(fromServer);
              }
              if (fromServer.equals("Goodbye")) {
                loop = false;
                break;
              }
            }
          } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
          } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
          }
        }
    }
}
