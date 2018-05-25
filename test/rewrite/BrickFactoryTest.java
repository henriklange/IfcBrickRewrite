package rewrite;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author henriklange
 */
public class BrickFactoryTest {

    EntityCollection testCollection;
    
    public BrickFactoryTest() {
        testCollection = new EntityCollection();
        testCollection.buildings.put(2, "building2");
        testCollection.floors.put(4, "floor4");
        testCollection.floors.put(5, "floor5");
        testCollection.rooms.put(8, "room8");
        testCollection.rooms.put(9, "room9");
        testCollection.rooms.put(11, "room11");
        testCollection.roofs.put(13, "roof13");
        testCollection.zones.put(16, "zone16");
        testCollection.zones.put(17, "zone17");
        
        //both floors in building2
        ArrayList<Integer> floorsInBuildings = new ArrayList();
        floorsInBuildings.add(4);
        floorsInBuildings.add(5);
        testCollection.relaggregates.put(2, floorsInBuildings);

        //both rooms on floor4
        ArrayList<Integer> roomsInFloors = new ArrayList();
        roomsInFloors.add(8);
        roomsInFloors.add(9);
        testCollection.relaggregates.put(4, roomsInFloors);
        
        //both rooms in zone 17
        ArrayList<Integer> roomsInZone = new ArrayList();
        roomsInZone.add(8);
        roomsInZone.add(9);
        testCollection.relAssignsToGroups.put(17, roomsInZone);
        
        //BrickFactory expects room-lookup.
        //This is enabled by the IFCFactory createRoomZoneLookup method
        IFCFactory ifcFactory = new IFCFactory();
        testCollection = ifcFactory.createRoomZoneLookup(testCollection);
        
    }

    /**
     * Test of writeBuildings method, of class BrickFactory.
     * writeBuildings prints buildings with associated floors
     * testCollection has Building2, with floor4 and floor5 associated.
     */
    @Test
    public void testWriteBuildings() {
        System.out.println("writeBuildings");
        BrickFactory factory = new BrickFactory();
        String expResult = "bldg:building2 a brick:Building ; " +
                            "bf:hasPart bldg:floor4 , " +
                            "bldg:floor5 . ";
        
        String result = factory.writeBuildings(testCollection);
        result = result.replaceAll("\\s+", " ");
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of writeFloors method, of class BrickFactory.
     * writeFloors prints floors with associated rooms
     * testCollection has floor4, with room8 and room9 associated.
     * room11 is not associated with a floor.
     */
    @Test
    public void testWriteFloors() {
        System.out.println("writeFloors");
        BrickFactory factory = new BrickFactory();
        String expResult = "bldg:floor4 a brick:Floor ; " +
                            "bf:hasPart bldg:room8 , " +
                            "bldg:room9 . " + 
                            "bldg:floor5 a brick:Floor . ";
        
        String result = factory.writeFloors(testCollection);
        result = result.replaceAll("\\s+", " ");
        
        assertEquals(expResult, result);
    }

    /**
     * Test of writeZones method, of class BrickFactory.
     * zone17 should be printed but zone 16 should not, as it holds no content.
     */
    @Test
    public void testWriteZones() {
        System.out.println("writeZones");
        BrickFactory factory = new BrickFactory();
        String expResult = "bldg:zone17 a brick:HVAC_Zone . ";
        
        String result = factory.writeZones(testCollection);
        result = result.replaceAll("\\s+", " ");
        
        assertEquals(expResult, result);
    }

    /**
     * Test of writeRooms method, of class BrickFactory.
     * Prints rooms along with the zones they are in.
     * testCollection has room8 and room9, both part of zone17.
     * zone16 is not used and should not be printed.
     * room11 is not in a zone.
     */
    @Test
    public void testWriteRooms() {
        System.out.println("writeRooms");
        BrickFactory factory = new BrickFactory();
        String expResult = "bldg:room8 a brick:Room ; " + 
                           "bf:isPartOf bldg:zone17 . " + 
                           "bldg:room9 a brick:Room ; " + 
                           "bf:isPartOf bldg:zone17 . " + 
                           "bldg:room11 a brick:Room . ";
        
        String result = factory.writeRooms(testCollection);
        result = result.replaceAll("\\s+", " ");
        
        assertEquals(expResult, result);
    }

    /**
     * Test of writeRoofs method, of class BrickFactory.
     */
    @Test
    public void testWriteRoofs() {
        System.out.println("writeRoofs");
        BrickFactory factory = new BrickFactory();
        String expResult = "bldg:roof13 a brick:Roof . ";
        
        String result = factory.writeRoofs(testCollection);
        result = result.replaceAll("\\s+", " ");
        
        assertEquals(expResult, result);
    }

    /**
     * Test of writeBrick method, of class BrickFactory.
     * writeBrick simply puts all the other methods together.
     * The test only checks if all methods has been run.
     */
    @Test
    public void testWriteBrick() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        System.out.println("writeBrick");
        BrickFactory instance = new BrickFactory();
        String expResult = "";
        String result = instance.writeBrick(testCollection);
        
        Assert.assertThat(result, CoreMatchers.containsString("@prefix"));
        Assert.assertThat(result, CoreMatchers.containsString("brick:Roof"));
        Assert.assertThat(result, CoreMatchers.containsString("brick:Room"));
        Assert.assertThat(result, CoreMatchers.containsString("brick:HVAC_Zone"));
        Assert.assertThat(result, CoreMatchers.containsString("brick:Building"));
        Assert.assertThat(result, CoreMatchers.containsString("brick:Floor"));
    }

    /**
     * Test of writePrefixes method, of class BrickFactory.
     */
    @Test
    public void testWritePrefixes() {
        System.out.println("writePrefixes");
        BrickFactory factory = new BrickFactory();
        String expResult = "@prefix bf: <https://brickschema.org/schema/1.0.1/BrickFrame#> .\n" +
        "@prefix bldg: <http://buildsys.org/ontologies/bldg#> .\n" +
        "@prefix brick: <https://brickschema.org/schema/1.0.1/Brick#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n";
        
        String result = factory.writePrefixes();
        assertEquals(expResult, result);
    }
    
}
