package rewrite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author henriklange
 */
public class Rewrite {

    public static String ifcFilename;
    
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        int loops = 1;
        //From Java Platform Performance: Strategies and Tactics"
        long startTime = System.currentTimeMillis();
        
        if(args.length == 0){
            ifcFilename = "/Users/henriklange/Desktop/School/SE10/IFCFiles/SDU_OU44/SDU OU44_C07.2_NV.ifc";
        } else {
            ifcFilename = args[0];
        }
        
        File file = new File(ifcFilename);
        if(file.exists() && !file.isDirectory()) { 

            
            String ifcFile = readFile(ifcFilename, StandardCharsets.UTF_8);

            EntityCollection entityCollection = new IFCFactory().parseIfc(ifcFile);

            String turtleData = new BrickFactory().writeBrick(entityCollection);

            String turtleFilename = ifcFilename.substring(0, ifcFilename.lastIndexOf(".")) + ".ttl";

            writeFile(turtleFilename, turtleData);
            
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("translated in " + elapsedTime + " ms");
            
        } else {
            System.err.println("File not found: '" + ifcFilename + "'");
        }
        if(true){
        //if(args.length == 3 && args[2].equals("halt")){
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        }
        
    }
    
    public static String readFile(String path, Charset encoding) {
        try{
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch(IOException e){
            System.err.println(e);
            return null;
        }
    }
    
    public static void writeFile(String turtleFilename, String turtleData){
        try (PrintWriter out = new PrintWriter(turtleFilename)) {
            out.print(turtleData);
        } catch (FileNotFoundException ex){
            System.err.println(ex);
        }
    }
}
