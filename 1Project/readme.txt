Anthony Dario
EECS 325
Fall 2015

This is a simple http proxy server set up to run on the current machine. This
Proxy support HTTP requests of all method types and HTTP responses using chunked
encoding or otherwise.

use "javac proxyd.java" to compile the code.
use "java proxyd -port <portnumber>" to start the server on the specified port.

After starting the server you can configure your browser to use it as a proxy
and you should be able to browse the web. The server will print some information
about the requests it is recieving to the console.  
