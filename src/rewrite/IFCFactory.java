package rewrite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

/**
 *
 * @author henriklange
 */
public class IFCFactory {
    
    String line = null;
    
    public EntityCollection parseIfc(String file){
        
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
    public EntityCollection interpretLines(String[] lines, EntityCollection entityCollection){
        try{
            
            for (String line : lines) {
                this.line = line;
                if(line.length() > 5){
                
                    int id = findId(line);
                    String definition = findDefinition(line);
                    //int id = Integer.parseInt(line.substring(line.indexOf("#") + 1, line.indexOf("=")).trim());

                    //String definition = line.substring(line.indexOf("I"), line.indexOf("("));
                    //System.out.println(id + " : " + definition);
                   
                    switch(definition){
                        case "ifcbuilding" :
                            entityCollection.buildings.put(id, "building" + id);
                        break;
                        case "ifcroof" :
                            entityCollection.roofs.put(id, "roof" + id);
                        break;
                        case "ifcgroup" :
                            entityCollection.zones.put(id, "zone" + id);
                        break;
                        case "ifczone" :
                            entityCollection.zones.put(id, "zone" + id);
                        break;
                        
                        case "ifcbuildingstorey" : 
                            // #id= ifcbuildingstorey('string',$,'STRING' ...);
                            entityCollection.floors.put(id, line
                                                    .split("'")[3]
                                                    .replace(" ", "_")
                                                    .replace(":", "_"));
                        break;
                        
                        case "ifcspace" : 
                            //#id= ifcspace('string',#id,'1',$,$,#id,#id,'NAME',.ELEMENT.,.INTERNAL.,$);
                            String name = "";
                            String[] strings = line.split("'");
                            if(strings.length >= 6){
                                name = strings[5] + strings[3];
                            } else {
                                name = strings[3];
                            }
                            
                            entityCollection.rooms.put(id, name
                                                    .replace(" ", "_")
                                                    .replace(":", "_"));
                            ;
                        break;
                        
                        case "ifcrelaggregates" : 
                            //#id= ifcrelaggrecates('string', #id,$,$,#ID,(ENTITYCOLLECTION)...);
                            //System.out.println(line);
                            ArrayList<Integer> entities = new ArrayList();
                            String contentCollection = line
                                    .split("\\(")[2];
                            contentCollection = contentCollection.substring(0, contentCollection.indexOf(")"));
                            
                            for(String eid : contentCollection.split(",")){
                                entities.add(Integer.parseInt(eid.replace("#", "")));
                            }
                            
                            entityCollection.relaggregates.put(Integer.parseInt(line.split(",")[4].replace("#", "")), entities);
                        break;
                        
                        case "ifcrelassignstogroup" :
                            //#269553= IFCRELASSIGNSTOGROUP('06B2afrsv0uBHvTl1DaaCm',
                            //#41,$,$,(#1666,#1700,#1759,#1784,#1822,#1856,#2198,#2264,#2328),$,#269552);
                            ArrayList<Integer> _entities = new ArrayList();
                            
                            String _contentCollection = line
                                    .split("\\(")[2];
                            _contentCollection = _contentCollection.substring(0, _contentCollection.indexOf(")"));
                            
                            //System.out.println(_entityCollection);
                            for(String eid : _contentCollection.split(",")){
                                _entities.add(Integer.parseInt(eid.replace("#", "")));
                            }
                            
                            String gid = line.substring(line.lastIndexOf("#") + 1);
                            
                            entityCollection.relAssignsToGroups.put(Integer.parseInt(gid), _entities);
                        ;
                        break;
                        
                        default:  /* nothing happens if the definition is irrelevant */ ;
                        
                    }
                    
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
