/*  
*   Dillon Latimore
*   TaxClient.java
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class TaxClient {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String msg;
        int port;
        String host;

        // Prompt client for default port or set their own
        System.out.println("Would you like to use the default port (4500)? y/n");
        String answer = scan.nextLine();
        if(answer.equals("y")) {
            port = 4500;
        } else {
            System.out.println("Please specify a port.");
            port = scan.nextInt();
            scan.nextLine();
        }

        // Prompt client for default host or set their own
        System.out.println("Would you like to use the default host (127.0.0.1)? y/n");
        answer = scan.nextLine();
        if(answer.equals("y")) {
            host = "127.0.0.1";
        } else {
            System.out.println("Please specify a host.");
            host = scan.nextLine();
        }
        
        
        while(true) {
            System.out.println("Enter a command");
            msg = scan.nextLine();
                // Attempts creating a client socket with default or chosen host and port
                try (
                    Socket s = new Socket(host, port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                ) {
                
                out.println(msg);
                msg = in.readLine();
                System.out.println(msg);

                // Main client loop
                while (true) {
                    System.out.println("Enter a command");
                    msg = scan.nextLine();
                    out.println(msg);

                    if(msg.equals("STORE")) {
                        msg = scan.nextLine();
                        out.println(msg);
                        msg = scan.nextLine();
                        out.println(msg);
                        msg = scan.nextLine();
                        out.println(msg);
                        msg = scan.nextLine();
                        out.println(msg);
                    }

                    if(msg.equals("QUERY")) {
                        while(true) {
                            msg = in.readLine();
                            System.out.println(msg);
                            if(msg.equals("QUERY: OK")) {
                                break;
                            }
                        }
                        continue;
                    }
                    
                    if(!msg.equals("QUERY: OK")) {
                        msg = in.readLine();
                    }
                    

                    switch(msg) {
                        
                            case "BYE: OK":
                            System.out.println(msg);
                            s.close();
                            break;
                        
                        case "END: OK":
                            System.out.println(msg);
                            s.close();
                            System.exit(0);
                            break;
                    
                        default:
                            System.out.println(msg);
                            break;
                    }
                    if(s.isClosed()) {
                        break;
                    }
                }
            
                } catch (Exception e) {
                    System.out.println("No server connection was made. Please try again.");
                    // e.printStackTrace();
                }
        }
    }
}
