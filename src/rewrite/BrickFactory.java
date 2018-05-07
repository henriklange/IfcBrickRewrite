package rewrite;

import java.util.ArrayList;

/**
 *
 * @author henriklange
 */
public class BrickFactory {
    
    boolean continuation = false;
    
    /**
     * Writes a Brick compatible Turtle string
     * from the contents of an entity collection.
     */
    public String writeBrick(EntityCollection entityCollection){
        
        String turtle = "";
        
        for(Integer building : entityCollection.buildings.keySet()){
            turtle += "bldg:" + entityCollection.buildings.get(building) + " a brick:Building ";
            if(entityCollection.relaggregates.containsKey(building)){
                ArrayList<Integer> rels = entityCollection.relaggregates.get(building);
                for(Integer rel : rels){
                    if(entityCollection.floors.containsKey(rel)){
                        if(continuation){
                            turtle += ", \n\nbldg:" + entityCollection.floors.get(rel) + " ";
                        } else {
                            turtle += "; \n\nbf:hasPart bldg:" + entityCollection.floors.get(rel) + " ";
                            continuation = true;
                        }
                    }
                }
            }
            turtle += " .\n\n";
            continuation = false;
        }
        
        for(Integer floor : entityCollection.floors.keySet()){
            turtle += "bldg:" + entityCollection.floors.get(floor) + " a brick:floor ";
            if(entityCollection.relaggregates.containsKey(floor)){
                ArrayList<Integer> rels = entityCollection.relaggregates.get(floor);
                for(Integer rel: rels){
                    if(entityCollection.rooms.containsKey(rel)){
                        if(continuation){
                            turtle += ",\n\n bldg:" + entityCollection.rooms.get(rel) + " ";
                        } else {
                            turtle += ";\n\n bf:hasPart bldg:" + entityCollection.rooms.get(rel) + " ";
                            continuation = true;
                        }
                    }
                }
            }
            turtle += " .\n\n";
            continuation = false;
        }
        
        for(Integer zone : entityCollection.usedZones.keySet()){
            turtle += "bldg:" + entityCollection.usedZones.get(zone) + " a brick:HVAC_Zone .\n\n";
        }
        
        for(Integer room: entityCollection.rooms.keySet()){
            turtle += "bldg:" + entityCollection.rooms.get(room) + " a brick:Room ";
            if(entityCollection.roomsInZones.containsKey(room)){
                //System.out.println("... bldg:" + zones.get(roomsInZones.get(room)) + " ...");
                turtle += ";\n\n\tbf:isPartOf bldg:" + entityCollection.zones.get(entityCollection.roomsInZones.get(room)) + " ";
            }
            turtle += " .\n\n";
        }
        
        for(Integer roof : entityCollection.roofs.keySet()){
            turtle += "bldg:" + entityCollection.roofs.get(roof) + " a brick:Roof .\n\n";
        }
        return turtle;
    }
    
    public String addPrefixes(String turtle){
        return "@prefix bf: <https://brickschema.org/schema/1.0.1/BrickFrame#> .\n" +
        "@prefix bldg: <http://buildsys.org/ontologies/bldg#> .\n" +
        "@prefix brick: <https://brickschema.org/schema/1.0.1/Brick#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n" + turtle;
    }
}
