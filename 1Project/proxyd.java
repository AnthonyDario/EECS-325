import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A simple web proxy that will forward HTTP requests and return the responses.
 * When running the proxy use the argument -port <port#> to specify which port 
 * to run the proxy on. 
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
    private void runServer(int remotePort, 
                           int localPort) 
                           throws IOException{

        // a count of the connections gone through this proxy
        int count = 0;

        // Socket that listens for communications
        ServerSocket localSocket = new ServerSocket(localPort);

        // the request and reply
        final byte[] requestStream = new byte[1024];
        byte[] replyStream = new byte[4096];

        // the server loop
        while (true){
    
            server = null;
            client = null;

            try {

                count++;

                // wait for a connection
                System.out.println("waiting for connection " + count + "...");
                client = localSocket.accept();
                System.out.println("connection " + count + " accepted");

                // Client streams
                final InputStream clientInput = client.getInputStream();
                final OutputStream clientOutput = client.getOutputStream();

                // forward the clients request to the server
                sendRequest(clientInput, clientOutput);
                                
                System.out.println("request handled\n");

            }
            catch (IOException e) {
                System.err.println(e);
            }
            // make sure that the sockets are closed
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
                    String host = "";
                    String method = "";
                    String[] headers;

                    // request is over when we hit a single blank line 
                    while (newLines < 4) {

                        byteRead = clientInput.read();

                        byteRequest.add((byte)byteRead);
 
                        if ((char) byteRead == '\n' || 
                            (char) byteRead == '\r') {
                            newLines++;
                        }
                        else {
                            newLines = 0;
                        }
                    }

                    // type manipulation
                    byte[] request = 
                        toPrimativeArray(byteRequest.toArray(new Byte[0]));

                    // get the requested server from the request
                    host = getHost(request);
                    method = getMethod(request);

                    System.out.println(method + " request for: " + host);

                    if (!method.equals("GET")) {
                        
                    }

                    // get the response from the host and write it to the client
                    response = recieveResponse(host, request);
                    clientOutput.write(response);
                    clientOutput.flush();

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
    private byte[] recieveResponse(String host, byte[] request) {
        
        try {
            server = new Socket(host, 80);
            System.out.println("made the socket to host: " + host);
           
            // get server Streams
            final InputStream serverInput = server.getInputStream();
            final OutputStream serverOutput = server.getOutputStream();

            // send the clients request
            serverOutput.write(request);
            serverOutput.flush();

            // get the servers response
            List<Byte> byteResponse = new ArrayList<Byte>(); 
            int byteRead;
            int newLines = 0;
            while (newLines < 4){

                byteRead = serverInput.read();

                byteResponse.add((byte)byteRead);

                if ((char) byteRead == '\n' || 
                    (char) byteRead == '\r') {
                    newLines++;
                }
                else {
                    newLines = 0;
                }

            }

            byte[] responseHeaders = 
                toPrimativeArray(byteResponse.toArray(new Byte[0]));
            byte[] responseBody = getBody(serverInput, responseHeaders);

            // close the server and return the response
            server.close();

            // get the full response
            byte[] response = concat(responseHeaders, responseBody);

            return response;

        }
        catch (Exception e) {
            System.out.println(e + "\nproxy could not resolve: " + host + "\n");
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
     * This method gets the request body from a stream
     * @param stream the stream to get the body from
     * @param headers the headers of the request
     * @return body the request body
     */
    private byte[] getBody(InputStream stream, byte[] headers) 
        throws IOException {

        // depending on headers, read the length of the response body 
        List<Byte> byteResponse = new ArrayList<Byte>(); 
        int contentLength;
        int byteRead;
        if ((contentLength = getContentLength(headers)) >= 0){

           for(int i = 0; i < contentLength; i++) {
               byteRead = stream.read();
               byteResponse.add((byte)byteRead);
           }
        }
        else if (isChunked(headers)) {

            int newLines = 0;
            while (newLines < 4){

                byteRead = stream.read();

                byteResponse.add((byte)byteRead);

                if ((char) byteRead == '\n' || 
                    (char) byteRead == '\r') {
                    newLines++;
                }
                else {
                    newLines = 0;
                }

            }
        }

        return toPrimativeArray(byteResponse.toArray(new Byte[0]));
    }

    /**
     * This gets the host from a request
     * @param request the request to parse
     * @return the host
     */
    private String getHost(byte[] request) {

        String headers = byteArrayToString(request);
        
        String[] headersArray = headers.split("\n");
        String host = headersArray[1].split(" ")[1];
        host = host.substring(0, host.length() - 1);

        return host;
    }

    /**
     * This gets the request method from a request
     * @param request the request to parse
     * @return the request method
     */
    private String getMethod(byte[] request) {

        String headers = byteArrayToString(request);

        String[] headersArray = headers.split("\n");
        String method = headersArray[0].split(" ")[0];

        return method;
    }

    /**
     * This returns the content length if the content length header exists, if 
     * there is no content length header then this will return -1
     * @param response the request we want the content length of
     * @return the length of the request body or -1 if the header doesn't exist
     */
    private int getContentLength(byte[] response) {

        int length = -1;
        String lengthString;
        String currentHeader;
        String[] headers = byteArrayToString(response).split("\n");

        for (int i = 0; i < headers.length; i++) {

            currentHeader = headers[i].split(" ")[0] ;

            if (currentHeader.equals("Content-Length:")) {
                lengthString = headers[i].split(" ")[1];
                lengthString = 
                    lengthString.substring(0, lengthString.length() - 1);

                length = Integer.parseInt(lengthString);
            }
        }

        return length;
    }

    /** 
     * This returns true if the content in the response is chunked
     * @param response the response being checked
     * @return true if the response is chunked
     */
    private boolean isChunked(byte[] response) {
        
        String currentHeader;
        String[] headers = byteArrayToString(response).split("\n");
        
        for(int i = 0; i < headers.length; i++) {
            currentHeader =  headers[i].substring(0, headers[i].length() - 1);
            if (currentHeader.equals("Transfer-Encoding: chunked")) {
                return true;
            }
        }
        return false;
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
     * This converts a Byte array into a string
     * @param array the byte array
     * @return The string
     */
    private String byteArrayToString(byte[] array) {

        String convert = "";

        for (int i = 0; i < array.length; i++) {
            convert += (char)array[i];
        }

        return convert;
    }

    /**
     * This concatenates two byte arrays into one
     * @param front the first array
     * @param back the second array
     */
    private byte[] concat(byte[] front, byte[] back) {

        byte[] full = new byte[front.length + back.length];

        for (int i = 0; i < front.length; i++) {
            full[i] = front[i];
        }

        for(int i = 0; i < back.length; i++) {
            full[i + front.length] = back[i];
        }

        return full;
    }

    /**
     * This starts up the server with the specified port
     * @param args -port <port#> specifies the port the server will run on
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        proxyd proxy = new proxyd();
        String host = "localhost";
        int remotePort = 100;
        int localPort = 5000;

        // determine the port to tun on
        if (args.length > 0 && args[0].equals("-port")) {
            localPort = Integer.parseInt(args[1]);
        }

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
