// Very very loosely vased on the Oracle sample "knockknockserver"
import java.net.*;
import java.io.*;
import java.sql.*;

public class FilmServer {

   public static void main(String[] args) throws IOException {
     Connection con = null;

     if (args.length != 1) {
       System.err.println("Usage: java FilmServer <port number>");
       System.exit(1);
     }
     // First connect to the "database"
     try {
       // CLASSPATH must be properly set, for instance on
       // a Linux system or a Mac:
       // $ export CLASSPATH=.:sqlite-jdbc-version-number.jar
       //
       Class.forName("org.sqlite.JDBC");
     } catch(Exception e) {
       System.err.println("Cannot find the driver.");
       System.exit(1);
     }
     try {
       con = DriverManager.getConnection("jdbc:sqlite:filmdb.sqlite");
       con.setAutoCommit(false);
       System.err.println("Successfully connected to the database.");
     } catch (Exception e) {
       System.err.println(e.getMessage());
       System.exit(1);
     }

     int            portNumber = Integer.parseInt(args[0]);
     String         inputLine, outputLine;
     FilmProtocol   filmP = new FilmProtocol(con);
     PrintWriter    out = null;
     BufferedReader in = null;
     ServerSocket   serverSocket = null;
     Socket         clientSocket = null;

     try { 
       serverSocket = new ServerSocket(portNumber);
       System.err.println("Film server started on port " + args[0]);
       while (true) {
          clientSocket = serverSocket.accept();
          System.err.println("Accepted connection");
          out =
            new PrintWriter(clientSocket.getOutputStream(), true);
          in = new BufferedReader(
                 new InputStreamReader(clientSocket.getInputStream()));
         // Wait for input
         if ((inputLine = in.readLine()) != null) {
           // System.err.println("From client: " + inputLine);
           outputLine = filmP.processInput(inputLine);
           // System.err.println("Sending back: " + outputLine);
           out.println(outputLine);
         }
         clientSocket.close();
       }
     } catch (IOException e) {
       System.out.println("Exception caught when trying to listen on port "
                  + portNumber + " or listening for a connection");
       System.out.println(e.getMessage());
     } catch (Exception e) {
       System.out.println(e.getMessage());
     } finally {
       if (out != null) {
         out.close();
       }
       if (in != null) {
         in.close();
       }
       if (clientSocket != null) {
         clientSocket.close(); 
       }
       if (serverSocket != null) {
         serverSocket.close(); 
       }
       if (con != null) {
         try {
           con.close();
         } catch (SQLException sqlE) {
           // Ignore
         }
       }
     }
  }
}

