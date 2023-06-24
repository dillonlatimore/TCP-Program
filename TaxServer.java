/*  
*   Dillon Latimore
*   TaxServer.java
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.ServerSocket;


public class TaxServer {

    // Data structure setup
    static String[] startArr = new String[10];
    static String[] endArr = new String[10];
    static String[] baseTaxArr = new String[10];
    static String[] taxArr = new String[10];

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        // setup tax storage data structure
        for(int i=0; i< 10; i++) {
            startArr[i] = "-1";
            endArr[i] = "-1";
            baseTaxArr[i] = "-1";
            taxArr[i] = "-1";
        }

        // Prompts for server port
        int port;
        System.out.println("Would you like to use the default port (4500)? y/n");
        String answer = scan.nextLine();
       
        if(answer.equals("y")) {
            port = 4500;
        } else {
            System.out.println("Please specify a port.");
            port = scan.nextInt();
        }
       


        while(true) {
            // Setup server socket and wait for client connect on same port
            try (
	        ServerSocket ss = new ServerSocket(port);
            Socket s = ss.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            ) 
            {
                // Main server loop
                String msg = in.readLine();
                System.out.println(msg);
                if(msg.equals("TAX")) {
                    out.println("TAX: OK");
                } else {
                    out.println("TAX message not received.");
                    continue;
                }
                    
                while (true) {
                    if(s.isClosed()) {
                        System.out.println("socket closed");
                        break;
                    }
                    msg = in.readLine();
                    System.out.println(msg);

                    try {
                        double result = taxCalc(Integer.parseInt(msg));
                        if(result == -1) {
                            out.println("I DON'T KNOW " + msg);
                            continue;
                        } else {
                            String resultString  = String.valueOf(result);
                            out.println("TAX IS " + resultString);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        
                    }

                    switch(msg) {
                        case "STORE":
                            String inStart = in.readLine();
                            System.out.println(inStart);
                            String inEnd = in.readLine();
                            System.out.println(inEnd);
                            String inBaseTax = in.readLine();
                            System.out.println(inBaseTax);
                            String inTax = in.readLine();
                            System.out.println(inTax);
                            store(inStart, inEnd, inBaseTax, inTax);
                            out.println("STORE: OK");
                            break;
                        
                        case "QUERY":
                            int n = 0;
                            for(int i = 0; i < startArr.length; i++) {
                                if(!startArr[i].equals("-1")) {
                                    n++;
                                }
                            }
                            
                            for(int i=0; i< n; i++) {
                                if(endArr[i].equals("2147483647")) {
                                    out.println(startArr[i] + " " + "~" + " " + baseTaxArr[i] + " " + taxArr[i]);
                                } else {
                                    out.println(startArr[i] + " " + endArr[i] + " " + baseTaxArr[i] + " " + taxArr[i]);
                                }
                                        
                            }
                            out.println("QUERY: OK");
                            break;
                        
                        case "BYE":
                            out.println("BYE: OK");
                            s.close();
                            break;
                        
                        case "END":
                            out.println("END: OK");
                            System.out.println("Closing Server");
                            Thread.sleep(2000);
                            ss.close();
                            System.exit(0);
                        break;
                    
                        default:
                            out.println("Invalid command.");
                            break;
                    }
                }
            }
            catch (Exception e) {  // You should have some better exception handling
                e.printStackTrace();
                break;
            }
        }
    
    }

    // stores the tax information in arrays, adapts based on existing entries
    private static void store(String start, String end, String baseTax, String tax) {
        
        if(end.equals("~")) {
            end = "2147483647";
        }
        
        //check for current entries
        int startArrConflict = -1;
        int endArrConflict = -1;
        boolean check = false;
        int fullCount = 0;
        for(int i=0; i<10; i++) {
            if(Integer.parseInt(startArr[i]) != -1) {
                fullCount++;
                check = true;
            }
        }

        if(fullCount == 10) {
            deleteAll();
            check = false;
        }

        // 1 =============================================================
        if(!check) {
            createEntry(start, end, baseTax, tax);
        // 1 =============================================================
        } else {
            // is start in range of another entry?
            startArrConflict = checkInRange(start);
            // 2 ============================================================= 
            if(startArrConflict != -1) {
                //is start the same?
                //check if start is the same as current entry start
                // 3 =============================================================
                if(isSame(start, startArrConflict, true)) {
                    // is end in range current?
                    // 4 =============================================================
                    endArrConflict = checkInRange(end);
                    if(endArrConflict == startArrConflict) {
                        //are ends the same?
                        // 5 =============================================================
                        if(isSame(end, endArrConflict, false)) {
                            deleteEntry(startArrConflict);
                            createEntry(start, end, baseTax, tax);
                        // 5 =============================================================
                        } else {
                            // 6 =============================================================
                            if(checkForRoom(1)) {
                                changeEntry(endArrConflict, true, Integer.parseInt(end)+1);
                                createEntry(start, end, baseTax, tax);  
                            // 6 =============================================================
                            } else {
                                deleteAll();
                                createEntry(start, end, baseTax, tax);
                            }                         
                        }
                    
                    // 4 =============================================================
                    } else {
                        //end is not in current

                        //is end in range of other entry?
                        // 7 =============================================================
                        if(endArrConflict != -1) {
                            // 8 =============================================================
                            if(endArr[endArrConflict].equals(end)) {
                                // 8a =============================================================
                                checkCovered(Integer.parseInt(start), Integer.parseInt(end));
                                deleteEntry(startArrConflict);
                                deleteEntry(endArrConflict);
                                createEntry(start, end, baseTax, tax);
                            // 8 =============================================================
                            } else {
                                // 9 =============================================================
                                readEntries();
                                checkCovered(Integer.parseInt(start), Integer.parseInt(end));
                                readEntries();
                                // 10 =============================================================
                                if(checkForRoom(1)) {
                                    changeEntry(endArrConflict, true, Integer.parseInt(end)+1);
                                    deleteEntry(startArrConflict);
                                    createEntry(start, end, baseTax, tax);
                                // 10 =============================================================
                                } else {
                                    deleteAll();
                                    createEntry(start, end, baseTax, tax);
                                }
                            }
                            
                        // 7 =============================================================  
                        } else {
                            checkCovered(Integer.parseInt(start), Integer.parseInt(end));
                            //overwrite whole thing
                            deleteEntry(startArrConflict);
                            createEntry(start, end, baseTax, tax);
                        }
                    }

                // 3 =============================================================    
                } else {
                    // is end in range of current
                    // 11 =============================================================
                    endArrConflict = checkInRange(end);
                    if(endArrConflict == startArrConflict) {
                        // is end the same?
                        // 12 =============================================================
                        if(isSame(end, startArrConflict, false)) {
                            // 13 =============================================================
                            if(checkForRoom(1)) {
                                changeEntry(startArrConflict, false, Integer.parseInt(start)-1);
                                createEntry(start, end, baseTax, tax);
                                
                            // 13 =============================================================
                            } else {
                                deleteAll();
                                createEntry(start, end, baseTax, tax);
                            }
                        
                        // 12 =============================================================
                        } else {
                            // 14 =============================================================
                            if(checkForRoom(2)) {
                                changeEntry(startArrConflict, false, Integer.parseInt(start)-1, Integer.parseInt(end)+1);
                                createEntry(start, end, baseTax, tax);
                            // 14 =============================================================
                            } else {
                                deleteAll();
                                createEntry(start, end, baseTax, tax);
                            }
                        }
                    // 11 =============================================================
                    } else {
                        // is end in range of anything?
                        // 15 =============================================================
                        if(endArrConflict != -1) {
                            // 16 =============================================================
                            checkCovered(Integer.parseInt(start), Integer.parseInt(end));

                            // 17 =============================================================
                            if(endArr[endArrConflict].equals(end)) {
                               changeEntry(startArrConflict, false, Integer.parseInt(start)-1);
                               deleteEntry(endArrConflict);
                               createEntry(start, end, baseTax, tax);
                            // 17 =============================================================
                            } else {
                                // 18 =============================================================
                                if(checkForRoom(1)) {
                                    changeEntry(startArrConflict, false, Integer.parseInt(start)-1);
                                    changeEntry(endArrConflict, true, Integer.parseInt(end)+1);
                                    createEntry(start, end, baseTax, tax);
                                    
                                // 18 =============================================================
                                } else {
                                    deleteAll();
                                    createEntry(start, end, baseTax, tax);
                                }
                            }

                            
                        // 15 =============================================================
                        } else {
                            // 19 =============================================================
                            checkCovered(Integer.parseInt(start), Integer.parseInt(end));

                            // 20 =============================================================
                            // end not in range of existing
                            if(checkForRoom(1)) {
                                changeEntry(startArrConflict, false, Integer.parseInt(start)-1);
                                createEntry(start, end, baseTax, tax);
                                
                            // 20 =============================================================
                            } else {
                                deleteAll();
                                createEntry(start, end, baseTax, tax);
                            }
                        }
                    }
                }

            // 2 =============================================================   
            } else {
                //start not in range of another entry

                //is end in range of another?
                endArrConflict = checkInRange(end);
                // 21 =============================================================
                if(endArrConflict != -1) {
                    // 23 =============================================================
                    checkCovered(Integer.parseInt(start), Integer.parseInt(end));
                    // 24 =============================================================
                    if(endArr[endArrConflict].equals(end)) {
                        // if end is equal to current end
                        deleteEntry(endArrConflict);
                        createEntry(start, end, baseTax, tax);
                    // 24 =============================================================
                    } else {
                        // 25 =============================================================
                        if(checkForRoom(1)) {
                            changeEntry(endArrConflict, true, Integer.parseInt(end)+1);
                            createEntry(start, end, baseTax, tax);
                        // 25 =============================================================
                        } else {
                            deleteAll();
                            createEntry(start, end, baseTax, tax);
                        }
                    }

                    
                // 21 =============================================================
                } else {
                    // 22 =============================================================
                    checkCovered(Integer.parseInt(start), Integer.parseInt(end));
                    createEntry(start, end, baseTax, tax);
                }
            }
        }
        selectionSort();
    }

    // creates a new tax entry
    private static void createEntry(String cStart, String cEnd, String cBaseTax, String cTax) {
        boolean flag = false;
        for(int i=0; i< 10; i++) {
            if(startArr[i].equals("-1")) {
                startArr[i] = cStart;
                endArr[i] = cEnd;
                baseTaxArr[i] = cBaseTax;
                taxArr[i] = cTax;
                flag = true;
            }
            if(flag) {
                break;
            }
        }
    }

    // logic for checking if tax entry is within an existing entry
    private static int checkInRange(String numToCheck) {
        int num = Integer.parseInt(numToCheck);
        int conflict = -1;
        for(int i=0; i< 10; i++) {
            int currentStart = Integer.parseInt(startArr[i]);
            int currentEnd = Integer.parseInt(endArr[i]);
            
            if(!startArr[i].equals("-1")) {
                if((num >= currentStart && num <= currentEnd) || 
                    (num >= currentStart && currentEnd == -55)) {
                    conflict = i;
                }
            }
        }
        return conflict;
    }

    // check if 2 values are the same
    private static boolean isSame(String numToCheck, int arrayConflict, boolean isStart) {
        int num = Integer.parseInt(numToCheck);
        
        if(isStart) {
            if(num == Integer.parseInt(startArr[arrayConflict])) {
                return true;
            } else {
                return false;
            }
        } else {
            if(num == Integer.parseInt(endArr[arrayConflict])) {
                return true;
            } else {
                return false;
            }
        }
    }

    // checks if there is sufficient space to save new tax entry
    private static boolean checkForRoom(int count) {
        for(int i=0; i< 10; i++) {
            if(startArr[i].equals("-1")) {
                count--;
            }
        }

        if(count <= 0) {
            return true;
        } else {
            return false;
        }
    }

    // alters an existing tax entry
    private static void changeEntry(int entryToChange, boolean isStart, int valueToChange) {
        if(isStart) {
            startArr[entryToChange] = Integer.toString(valueToChange);
        } else {
            endArr[entryToChange] = Integer.toString(valueToChange);
        }
    }

    // alters an existing tax entry
    private static void changeEntry(int entryToChange, boolean isStart, int valueToChange, int createStartValue) {
        
        String oldEnd = endArr[entryToChange];
        if(isStart) {
            startArr[entryToChange] = Integer.toString(valueToChange);
        } else {
            endArr[entryToChange] = Integer.toString(valueToChange);
        }
        
        createEntry(Integer.toString(createStartValue), oldEnd, baseTaxArr[entryToChange], taxArr[entryToChange]);

    }

    // deletes all tax entries
    private static void deleteAll() {
        for(int i=0; i< 10; i++) {
            startArr[i] = "-1";
            endArr[i] = "-1";
            baseTaxArr[i] = "-1";
            taxArr[i] = "-1";
        }
    }

    // deletes a chosen tax entry
    private static void deleteEntry(int selected) {
        startArr[selected] = "-1";
        endArr[selected] = "-1";
        baseTaxArr[selected] = "-1";
        taxArr[selected] = "-1";
    }

    // checks if a tax entry in covered by a new tax entry
    private static void checkCovered(int start, int end) {
        for(int i=0; i< 10; i++) {
            // completely covered
            if((Integer.parseInt(startArr[i]) > start && Integer.parseInt(endArr[i]) < end)) {
                deleteEntry(i);
            }
        }
    }

    // prints out all tax entries
    private static void readEntries() {
        for(int i=0; i< 10; i++) {
            System.out.println(startArr[i]);
            if(endArr[i].equals("2147483647")) {
                System.out.println("~");
            } else {
                System.out.println(endArr[i]);
            }
            System.out.println(baseTaxArr[i]);
            System.out.println(taxArr[i]);
        }
    }

    // sorts tax entries in ascending order using selection sort
    private static void selectionSort() {
    
        int n = 0;
        for(int i = 0; i < startArr.length; i++) {
            if(!startArr[i].equals("-1")) {
                n++;
            }
        }

        if(n > 1) {
            for (int i = 0; i < n-1; i++) {

                int min = i;
                for (int j = i+1; j < n; j++) {
                    if (Integer.parseInt(startArr[j]) < Integer.parseInt(startArr[min])) {
                        min = j;
                    }
                }

                String startTemp = startArr[min];
                String endTemp = endArr[min];
                String baseTaxTemp = baseTaxArr[min];
                String taxTemp = taxArr[min];
                
                startArr[min] = startArr[i];
                endArr[min] = endArr[i];
                baseTaxArr[min] = baseTaxArr[i];
                taxArr[min] = taxArr[i];
    
                startArr[i] = startTemp;
                endArr[i] = endTemp;
                baseTaxArr[i] = baseTaxTemp;
                taxArr[i] = taxTemp;
            }
        }
        
    }

    // performs tax calculation on provided value based off existing tax entries
    private static double taxCalc(int input) {
        
        double startVal;
        double endVal;
        for(int i=0; i< 10; i++) { 
            if(!startArr[i].equals("-1")) {
                startVal = Double.parseDouble(startArr[i]);
                endVal = Double.parseDouble(endArr[i]);
                
                if((input >= startVal && input <= endVal)) {
                    double baseTaxVal = Double.parseDouble(baseTaxArr[i]);
                    String taxString = "0." + taxArr[i];
                    double taxVal = Double.parseDouble(taxString);
                    double taxAmount = baseTaxVal + ((input - startVal) * taxVal);
                    return taxAmount;
                }
            }
        }


        return -1;
    }
}
    
