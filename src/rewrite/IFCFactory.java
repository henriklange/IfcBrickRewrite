package rewrite;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.reflect.Method;

/**
 * @author henriklange
 * 
 * Finds Brick-compatible data in IFC files
 * and stores it in an EntityCollection.
 * 
 */
public class IFCFactory {
    
    String line = null;
    IFCEntityFactory ifcEntityFactory = new IFCEntityFactory();
    
    public EntityCollection parseIfc(String file) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        file = extractData(file);
        file = removeLineBreaks(file);
        String[] lines = splitData(file);
        
        EntityCollection entityCollection = new EntityCollection();
        entityCollection = interpretLines(lines, entityCollection);
        entityCollection = createRoomZoneLookup(entityCollection);
        
        return entityCollection;
    }
    
    /**
     * Extracts the data section of an IFC file.
     */
    public String extractData(String file){
        try{
            file = file.split("DATA;")[1]; //Everything before this point is meta data
            file = file.split("ENDSEC;")[0]; //Everything after this point is meta data
            return file;
        } catch (StringIndexOutOfBoundsException ex) {
            System.err.println("IFC id "+ /*lastId +*/" format exception: " + ex.fillInStackTrace());
            System.err.println("Problem with line: " + line);
        }
        return null;
    }
    
    /**
     * Removes All Line Breaks From a String.
     */
    public String removeLineBreaks(String file){
        return file.replaceAll("\r\n", "").replaceAll("\n", "");
    }
    
    /**
     * Splits IFC data string into an array of strings,
     * each containing one line of IFC data.
     */
    public String[] splitData(String data){
        return data.split("[\\)][\\s]*[;]");
    }
    
    /**
     * Finds the ID of an IFC component.
     * #4 = IFCEXAMPLE(); returns 4
     */
    public int findId(String line){
        return Integer.parseInt(line.substring(line.indexOf("#") + 1, line.indexOf("=")).trim());
    }
    
    /**
     * Finds the definition of an IFC component.
     * #4 = IFCEXAMPLE(); returns "IFCEXAMPLE"
     */
    public String findDefinition(String line){
        return line.substring(line.toLowerCase().indexOf("i"), line.indexOf("(")).toLowerCase();
    }
    
    /**
     * Populates an Entity Collection with the content of ifc lines.
     */
    public EntityCollection interpretLines(String[] lines, EntityCollection entityCollection) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        try{
            
            for (String line : lines) {
                this.line = line;
                if(line.length() > 5){
                
                    int id = findId(line);
                    String definition = findDefinition(line);
                    
                    entityCollection = ifcEntityFactory.dispatch(id, definition, line, entityCollection);
                    
                }
            }
            
            
        } catch (StringIndexOutOfBoundsException ex) {
            System.err.println("IFC id "+ /*lastId +*/" format exception: " + ex.fillInStackTrace());
            System.err.println("Problem with line: " + line);
        }
        return entityCollection;
    }
    
    /**
     * Reverse mapping of rooms and zones.
     * This means that zones can be found by looking up rooms,
     * instead of constant traversal by looking for rooms within all zones.
     */
    public EntityCollection createRoomZoneLookup(EntityCollection entityCollection){
        //enables room lookup for groups/zones
        for(Integer zone: entityCollection.zones.keySet()){
            if(entityCollection.relAssignsToGroups.containsKey(zone)){
                ArrayList<Integer> zoneContent = entityCollection.relAssignsToGroups.get(zone);
                for(Integer room: zoneContent){
                    if(entityCollection.rooms.containsKey(room)){
                        entityCollection.roomsInZones.put(room, zone);
                        if(!entityCollection.usedZones.containsKey(zone)){
                            entityCollection.usedZones.put(zone, entityCollection.zones.get(zone));
                        }
                    }
                }
            }
        }
        return entityCollection;
    }
    
    
}
