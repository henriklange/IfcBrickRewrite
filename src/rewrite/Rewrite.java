package rewrite;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author henriklange
 */
public class Rewrite {

    public static void main(String[] args) {
        
        String ifcFilename = "/Users/henriklange/Desktop/School/SE10/IFC Files/testbuildings//Paolo.ifc";
        String ifcFile = readFile(ifcFilename, StandardCharsets.UTF_8);
        IFCReader ifcReader = new IFCReader();
        
        ifcReader.parseIfc(ifcFile);
        String turtle = ifcReader.writeBrick();
        
        String turtleFilename = ifcFilename.substring(0, ifcFilename.lastIndexOf(".")) + ".ttl";

        try (PrintWriter out = new PrintWriter(turtleFilename)) {
            out.print(turtle);
        } catch (FileNotFoundException ex){
            System.err.println(ex);
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
}
