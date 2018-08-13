package edu.coursera.distributed;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
            final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
    	while (true) {

            // TODO Delete this once you start working on your solution.

            // TODO 1) Use socket.accept to get a Socket object
        	
        	Socket theSocket = socket.accept();

            /*
             * TODO 2) Using Socket.getInputStream(), parse the received HTTP
             * packet. In particular, we are interested in confirming this
             * message is a GET and parsing out the path to the file we are
             * GETing. Recall that for GET HTTP packets, the first line of the
             * received packet will look something like:
             *
             *     GET /path/to/file HTTP/1.1
             */
        	Thread t = new Thread ( ()-> {

                /*
                 * TODO 2) Using Socket.getInputStream(), parse the received HTTP
                 * packet. In particular, we are interested in confirming this
                 * message is a GET and parsing out the path to the file we are
                 * GETing. Recall that for GET HTTP packets, the first line of the
                 * received packet will look something like:
                 *
                 *     GET /path/to/file HTTP/1.1
                 */
        		InputStream stream = null;
        		InputStreamReader reader = null;
        		OutputStream out = null;
        		PrintWriter printer = null;
        		try {
    				stream = theSocket.getInputStream();

                	reader = new InputStreamReader(stream);
                	BufferedReader buffered = new BufferedReader(reader);
                	
                	String line = null;
    				line = buffered.readLine();
                	assert line != null;
                	assert line.startsWith("GET");
                	final String path = line.split(" ")[1];
                	
                	String contents = fs.readFile(new PCDPPath(path));            	
                	
    				out = theSocket.getOutputStream();
    	            printer = new PrintWriter(out);
                	
                	if (contents != null) {
    					printer.write("HTTP/1.0 200 OK\r\n");
    					printer.write("Server: FileServer\r\n");
    					printer.write("\r\n");
    					printer.write( contents +"\r\n");
                	} else {
                		printer.write("HTTP/1.0 404 Not Found\r\n");
                		printer.write("Server: FileServer\r\n");
                		printer.write("\r\n");
                	}
        		} catch (IOException e) {					
					e.printStackTrace();
					return;
				} finally {
            	printer.close();
        		}
        	});
        	t.start();
        }
    }
}
