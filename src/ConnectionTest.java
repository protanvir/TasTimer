import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import java.util.Scanner;

public class ConnectionTest {
    public static void main(String[] args) {
        // Use provided IP as default, but allow override via args
        String hostname = "172.16.1.114"; 
        if (args.length > 0) {
            hostname = args[0];
        }

        System.out.println("Attempting to connect to " + hostname);
        ImpinjReader reader = new ImpinjReader();

        try {
            // The connect() method creates the LLRP connection to the reader
            reader.connect(hostname);
            System.out.println("Result: Connected successfully to " + hostname);
            
            // Keep connection open briefly or wait for user input if interactive, 
            // but for this automated test script, we'll just disconnect immediately
            // to prove it works.
            System.out.println("Disconnecting...");
            reader.disconnect();
            System.out.println("Done.");
            
        } catch (OctaneSdkException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
             System.err.println("An unexpected error occurred: " + e.getMessage());
             e.printStackTrace();
        }
    }
}
