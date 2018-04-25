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
public class IFCReader {
    
    HashMap<Integer, String> buildings = new HashMap();
    HashMap<Integer, String> floors = new HashMap();
    HashMap<Integer, String> rooms = new HashMap();
    HashMap<Integer, ArrayList> relaggregates = new HashMap();

    HashMap<Integer, String> roofs = new HashMap();
    HashMap<Integer, String> zones = new HashMap();
    HashMap<Integer, String> usedZones = new HashMap();
    HashMap<Integer, ArrayList> relAssignsToGroups = new HashMap();

    HashMap<Integer, Integer> roomsInZones = new HashMap();
    
    boolean continuation = false;
    
    HashMap<String, Integer> ifcQuantities = new HashMap<>();
    
    String line = null;
    
    public void parseIfc(String file){
    
        try{
            file = file.split("DATA;")[1]; //Everything before this point is meta data
            file = file.split("ENDSEC;")[0]; //Everything after this point is meta data
            
            file = file.replaceAll("\r\n", "").replaceAll("\n", "");
            
            String[] lines = file.split("[\\)][\\s]*[;]");
            
            for (String line : lines) {
                this.line = line;
                if(line.length() > 5){
                
                    int id = Integer.parseInt(line.substring(line.indexOf("#") + 1, line.indexOf("=")).trim());

                    String definition = line.substring(line.indexOf("I"), line.indexOf("("));
                    //System.out.println(id + " : " + definition);
                   
                    switch(definition.toLowerCase()){
                        case "ifcbuilding" :
                            buildings.put(id, "building" + id);
                        break;
                        case "ifcroof" :
                            roofs.put(id, "roof" + id);
                        break;
                        case "ifcgroup" :
                            zones.put(id, "zone" + id);
                        break;
                        case "ifczone" :
                            zones.put(id, "zone" + id);
                        break;
                        
                        case "ifcbuildingstorey" : 
                            // #id= ifcbuildingstorey('string',$,'STRING' ...);
                            floors.put(id, line
                                            .split("'")[3]
                                            .replace(" ", "_")
                                            .replace(":", "_"));
                        break;
                        
                        case "ifcspace" : 
                            String name = "";
                            String[] strings = line.split("'");
                            if(strings.length >= 6){
                                name = strings[5] + strings[3];
                            } else {
                                name = strings[3];
                            }
                            
                            rooms.put(id, name
                                .replace(" ", "_")
                                .replace(":", "_"));
                            ;
                        break;
                        
                        case "ifcrelaggregates" : 
                            //#id= ifcrelaggrecates('string', #id,$,$,#ID,(ENTITYCOLLECTION)...);
                            //System.out.println(line);
                            ArrayList<Integer> entities = new ArrayList();
                            String entityCollection = line
                                    .split("\\(")[2];
                            entityCollection = entityCollection.substring(0, entityCollection.indexOf(")"));
                            
                            for(String eid : entityCollection.split(",")){
                                entities.add(Integer.parseInt(eid.replace("#", "")));
                            }
                            
                            relaggregates.put(Integer.parseInt(line.split(",")[4].replace("#", "")), entities);
                        break;
                        
                        case "ifcrelassignstogroup" :
                            //#269553= IFCRELASSIGNSTOGROUP('06B2afrsv0uBHvTl1DaaCm',
                            //#41,$,$,(#1666,#1700,#1759,#1784,#1822,#1856,#2198,#2264,#2328),$,#269552);
                            ArrayList<Integer> _entities = new ArrayList();
                            
                            String _entityCollection = line
                                    .split("\\(")[2];
                            _entityCollection = _entityCollection.substring(0, _entityCollection.indexOf(")"));
                            
                            //System.out.println(_entityCollection);
                            for(String eid : _entityCollection.split(",")){
                                _entities.add(Integer.parseInt(eid.replace("#", "")));
                            }
                            
                            String gid = line.substring(line.lastIndexOf("#") + 1);
                            
                            relAssignsToGroups.put(Integer.parseInt(gid), _entities);
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
        
        //enables room lookup for groups/zones
        for(Integer zone: zones.keySet()){
            if(relAssignsToGroups.containsKey(zone)){
                ArrayList<Integer> zoneContent = relAssignsToGroups.get(zone);
                for(Integer room: zoneContent){
                    if(rooms.containsKey(room)){
                        roomsInZones.put(room, zone);
                        if(!usedZones.containsKey(zone)){
                            usedZones.put(zone, zones.get(zone));
                        }
                    }
                }
            }
        }
        
    }
    
    public String writeBrick(){
        
        String turtle = 
        "@prefix bf: <https://brickschema.org/schema/1.0.1/BrickFrame#> .\n" +
        "@prefix bldg: <http://buildsys.org/ontologies/bldg#> .\n" +
        "@prefix brick: <https://brickschema.org/schema/1.0.1/Brick#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n";
        
        for(Integer building : buildings.keySet()){
            turtle += "bldg:" + buildings.get(building) + " a brick:Building ";
            if(relaggregates.containsKey(building)){
                ArrayList<Integer> rels = relaggregates.get(building);
                for(Integer rel : rels){
                    if(floors.containsKey(rel)){
                        if(continuation){
                            turtle += ", \n\nbldg:" + floors.get(rel) + " ";
                        } else {
                            turtle += "; \n\nbf:hasPart bldg:" + floors.get(rel) + " ";
                            continuation = true;
                        }
                    }
                }
            }
            turtle += " .\n\n";
            continuation = false;
        }
        
        for(Integer floor : floors.keySet()){
            turtle += "bldg:" + floors.get(floor) + " a brick:floor ";
            if(relaggregates.containsKey(floor)){
                ArrayList<Integer> rels = relaggregates.get(floor);
                for(Integer rel: rels){
                    if(rooms.containsKey(rel)){
                        if(continuation){
                            turtle += ",\n\n bldg:" + rooms.get(rel) + " ";
                        } else {
                            turtle += ";\n\n bf:hasPart bldg:" + rooms.get(rel) + " ";
                            continuation = true;
                        }
                    }
                }
            }
            turtle += " .\n\n";
            continuation = false;
        }
        
        for(Integer zone : usedZones.keySet()){
            turtle += "bldg:" + usedZones.get(zone) + " a brick:HVAC_Zone .\n\n";
        }
        
        for(Integer room: rooms.keySet()){
            turtle += "bldg:" + rooms.get(room) + " a brick:Room ";
            if(roomsInZones.containsKey(room)){
                //System.out.println("... bldg:" + zones.get(roomsInZones.get(room)) + " ...");
                turtle += ";\n\n\tbf:isPartOf bldg:" + zones.get(roomsInZones.get(room)) + " ";
            }
            turtle += " .\n\n";
        }
        
        for(Integer roof : roofs.keySet()){
            turtle += "bldg:" + roofs.get(roof) + " a brick:Roof .\n\n";
        }
        
        return turtle;
    }
 
    public void clear(){
        buildings.clear();
        floors.clear();
        rooms.clear();
        relaggregates.clear();

        roofs.clear();
        zones.clear();
        usedZones.clear();
        relAssignsToGroups.clear();

        roomsInZones.clear();

        continuation = false;
    }
    
}
