package rewrite;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author henriklange
 */
public class EntityCollection {
    public HashMap<Integer, String> buildings = new HashMap();
    public HashMap<Integer, String> floors = new HashMap();
    public HashMap<Integer, String> rooms = new HashMap();
    public HashMap<Integer, ArrayList> relaggregates = new HashMap();

    public HashMap<Integer, String> roofs = new HashMap();
    public HashMap<Integer, String> zones = new HashMap();
    public HashMap<Integer, String> usedZones = new HashMap();
    public HashMap<Integer, ArrayList> relAssignsToGroups = new HashMap();

    public HashMap<Integer, Integer> roomsInZones = new HashMap();
    
    public HashMap getBuildings(){
        return buildings;
    }
    
    public HashMap getFloors(){ 
        return floors;
    }
    
    public HashMap getRooms(){
        return rooms;
    }
    
    public HashMap getRoofs(){
        return roofs;
    }
    
    public void addRelAggregates(int containerId, ArrayList contents){
        this.relaggregates.put(containerId, contents);
    }
}
