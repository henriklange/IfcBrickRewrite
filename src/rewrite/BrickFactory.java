package rewrite;

import java.util.ArrayList;

/**
 * @author henriklange
 * Creates a Brick compatible string from the data in an EntityCollection.
 * 
 */
public class BrickFactory {
    
    boolean continuation = false;
    
    /**
     * Writes Brick buildings and associates floors from EntityCollections.
     * @param entityCollection
     * @return buildings with floors in EntityCollection as Brick String.
     */
    public String writeBuildings(EntityCollection entityCollection){
        
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
        return turtle;
    }
    
    /**
     * Writes Brick floors from EntityCollections.
     * @param entityCollection
     * @return floors with associated rooms in EntityCollection as Brick String.
     */
    public String writeFloors(EntityCollection entityCollection){
        
        String turtle = "";

        for(Integer floor : entityCollection.floors.keySet()){
            turtle += "bldg:" + entityCollection.floors.get(floor) + " a brick:Floor ";
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
        
        return turtle;
        
    }
    
    /**
     * Defines Brick zones from EntityCollections.
     * @param entityCollection
     * @return zones in EntityCollection as Brick String.
     */
    public String writeZones(EntityCollection entityCollection){
        String turtle = "";
        for(Integer zone : entityCollection.usedZones.keySet()){
            turtle += "bldg:" + entityCollection.usedZones.get(zone) + " a brick:HVAC_Zone .\n\n";
        }
        return turtle;
    }
    
    /**
     * Defines Brick rooms and associates zones from EntityCollections.
     * @param entityCollection
     * @return rooms and associated zones in EntityCollection as Brick String.
     */
    public String writeRooms(EntityCollection entityCollection){    
        String turtle = "";
        for(Integer room: entityCollection.rooms.keySet()){
            turtle += "bldg:" + entityCollection.rooms.get(room) + " a brick:Room ";
            if(entityCollection.roomsInZones.containsKey(room)){
                //System.out.println("... bldg:" + zones.get(roomsInZones.get(room)) + " ...");
                turtle += ";\n\n\tbf:isPartOf bldg:" + entityCollection.zones.get(entityCollection.roomsInZones.get(room)) + " ";
            }
            turtle += " .\n\n";
        }
        return turtle;
    }
    
    /**
     * Writes Brick roofs from EntityCollections.
     * @param entityCollection
     * @return roofs in EntityCollection as Brick String.
     */
    public String writeRoofs(EntityCollection entityCollection){
        String turtle = "";
        for(Integer roof : entityCollection.roofs.keySet()){
            turtle += "bldg:" + entityCollection.roofs.get(roof) + " a brick:Roof .\n\n";
        }
        return turtle;
    }
    
    /**
     * Writes a Brick compatible Turtle string
     * from the contents of an entity collection.
     */
    public String writeBrick(EntityCollection entityCollection){
        
        String turtle = writePrefixes();
        turtle += writeBuildings(entityCollection);
        turtle += writeFloors(entityCollection);
        turtle += writeZones(entityCollection);
        turtle += writeRooms(entityCollection);
        turtle += writeRoofs(entityCollection);
    
        return turtle;
    }
    
    /**
     * Writes a static set of prefixes for Brick files.
     */
    public String writePrefixes(){
        return "@prefix bf: <https://brickschema.org/schema/1.0.1/BrickFrame#> .\n" +
        "@prefix bldg: <http://buildsys.org/ontologies/bldg#> .\n" +
        "@prefix brick: <https://brickschema.org/schema/1.0.1/Brick#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n";
    }
}
