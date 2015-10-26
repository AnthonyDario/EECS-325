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
                sendRequest(clientInput, clientOutput);
                                
                System.out.println("\nrequest sent\n");

            }
            catch (IOException e) {
                System.err.println(e);
            }
            finally {
                try{
                    if (server != null) {
                        server.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                }
                catch(IOException e) {
                    System.err.println(e);
                }

            }

        }

    }

    /**
     * This sends a clients request to the server
     * @param clientInput the input stream from the client
     */
    private void sendRequest(InputStream clientInput, 
                             OutputStream clientOutput) {

        // Send the clients request to the server on a seperate thread
        Thread clientToServer = new Thread() {

            public void run() {

                try {
                    int byteRead;
                    int newLines = 0;
                    byte[] response;
                    List<Byte> byteRequest = new ArrayList<Byte>();
                    String request = "";
                    String host = "";
                    String[] headers;

                    while ((byteRead = clientInput.read()) >= 0) {

                        byteRequest.add((byte)byteRead);
                        request += (char)byteRead;
 
                        if ((char) byteRead == '\n' || 
                            (char) byteRead == '\r') {
                            newLines++;
                        }
                        else {
                            newLines = 0;
                        }
                        if (newLines == 4) {
                            System.out.println("\n" + request);
                            headers = request.split("\n");
                            host = headers[1].split(" ")[1];
                            host = host.substring(0, host.length() - 1);
                            Byte[] requestArray = 
                                byteRequest.toArray(new Byte[0]);

                            response = recieveResponse(host, requestArray);

                            System.out.println("got response: \n");
                            for(int i = 0; i < response.length; i++) {
                                System.out.print((char)response[i]);
                            }

                            clientOutput.write(response);
                            clientOutput.flush();
                            System.out.println("wrote to client");

                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                finally {
                    try {
                        client.close(); 
                    }
                    catch (IOException e) {
                        System.out.println("client.close() messed up: " + e);
                    }
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
    private byte[] recieveResponse(String host, Byte[] request) {
        
        try {
            byte[] byteRequest = toPrimativeArray(request);
            server = new Socket(host, 80);
            System.out.println("made the socket");
           
            // get server Streams
            final InputStream serverInput = server.getInputStream();
            final OutputStream serverOutput = server.getOutputStream();

            // send the clients request
            serverOutput.write(byteRequest);
            serverOutput.flush();

            // get the servers response
            String response = "";
            List<Byte> byteResponse = new ArrayList<Byte>(); 
            int byteRead;
            int newLines = 0;
            int times = 0;
            while ((byteRead = serverInput.read()) >= 0){

                System.out.print((char)byteRead);
                response += (char)byteRead;
                byteResponse.add((byte)byteRead);

                if ((char) byteRead == '\n' || 
                    (char) byteRead == '\r') {
                    newLines++;
                }
                else {
                    newLines = 0;
                }

                if (newLines == 4) {
                    times++;
                    if (times == 2) {
                        server.close();

                        return toPrimativeArray(
                                    byteResponse.toArray(new Byte[0])
                               );
                    }
                }
            }

            return toPrimativeArray(byteResponse.toArray(new Byte[0]));
        }
        catch (Exception e) {
            System.out.println(e + "proxy could not resolve: " + host);
            return new byte[0];
        }
        finally {
            try {
                server.close();
            }
            catch (IOException e) {
                System.out.println("server.close() messed up: " + e);
            }
        }

    }

    /**
     * This converts a Byte array into an array of bytes
     * @param array The array of Bytes
     * @return The array converted to bytes
     */
    private byte[] toPrimativeArray(Byte[] input) {

        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
           output[i] = input[i].byteValue();
        }

        return output;
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
