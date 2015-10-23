import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A simple web proxy
 * @author Anthony Dario
 */
public class proxyd {

    private Socket client;
    private Socket server;
    /**
     * This will start the server
     *
     * @param host          The host of the proxy
     * @param remotePort    The outward facing port
     * @param localPort     The localPort for the proxy
     */
    private void runServer(int remotePort, int localPort) 
                                  throws IOException{

        int count = 0;
        // Socket that listens for communications
        ServerSocket localSocket = new ServerSocket(localPort);

        // the request and reply
        final byte[] requestStream = new byte[1024];
        byte[] replyStream = new byte[4096];

        System.out.println("starting loop");
        // the server loop
        while (true){
    
            server = null;
            client = null;

            try {

                // wait for a connection
                System.out.println("waiting for connection " + count + "...");
                client = localSocket.accept();
                System.out.println("connection " + count + " accepted");

                // Client streams
                final InputStream clientInput = client.getInputStream();
                final OutputStream clientOutput = client.getOutputStream();

                // forward the clients request to the server
                sendRequest(clientInput);
                                

                // Server streams
                //final InputStream serverInput = server.getInputStream();
                //final OutputStream serverOutput = server.getOutputStream();


            }
            catch (IOException e) {
                System.out.println("catch block");
                System.err.println(e);
            }
            finally {
                System.out.println("finally block");
                try{
                    if (server != null) {

                        server.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                }
                catch(IOException e) {
                    System.out.println("catch block");
                    System.err.println(e);
                }

            }

        }

    }

    /**
     * This sends a clients request to the server
     * @param clientInput the input stream from the client
     */
    private void sendRequest(InputStream clientInput) {

        // Send the clients request to the server on a seperate thread
        Thread clientToServer = new Thread() {

            public void run() {

                try {
                    int byteRead;
                    int newLines = 0;
                    List<Byte> byteRequest = new ArrayList<Byte>();
                    String request = "";
                    String host = "";
                    String[] headers;

                    while ((byteRead = clientInput.read()) >= 0) {

                        byteRequest.add((byte)byteRead);
                        request += (char)byteRead;
 
                        if ((char) byteRead == '\n' || (char) byteRead == '\r') {
                            newLines++;
                        }
                        else {
                            newLines = 0;
                        }
                        if (newLines == 4) {
                            System.out.println(request);
                            headers = request.split("\n");
                            host = headers[1].split(" ")[1];
                            host = host.substring(0, host.length() - 1);
                            Byte[] requestArray = byteRequest.toArray(new Byte[0]);
                            recieveResponse(host, requestArray);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                finally {
                    client.close(); 
                }

            }
        };

        // send the clients request
        clientToServer.run();
    }

    /**
     * This creates a socket for the given host and port
     * @param host The host for the socket
     * @param port the port for the socket
     */
    private void recieveResponse(String host, Byte[] request) {
        
        try {
            server = new Socket(host, 80);
            System.out.println("MADE THE SOCKET SUCKA");
           
            // get server Streams
            final InputStream serverInput = server.getInputStream();
            final OutputStream serverOutput = server.getOutputStream();

            // send the clients request
            serverInput.write(request);
            serverInput.flush();

            // get the servers response
            String response = "";
            byte[] response = new byte[4096];
            int bytesRead;
            while (byteRead = serverOutput.read() >= 0){
                
            }
            System.out.println(response);
        }
        catch (Exception e) {
            System.out.println(e + "proxy could not resolve: " + host);
        }
        finally {
            server.close();
        }

    }

    /**
     * This converts a Byte array into an array of bytes
     * @param array The array of Bytes
     * @return The array converted to bytes
     */
    private byte[] toPrimativeArray(Byte[] array) {

    }

    /**
     * This starts up the server with the specified port
     * @param args -port <port#> specifies the port the server will run on
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        proxyd proxy = new proxyd();
        // Constants
        String host = "localhost";
        int remotePort = 100;
        int localPort = 5000;

        // Start-up message
        System.out.println("Starting proxy for " + host + ":" + remotePort
                + " on port " + localPort);

        // run the server
        try {
            proxy.runServer(remotePort, localPort);

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
