package rewrite;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import static jdk.nashorn.internal.objects.Global.instance;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author henriklange
 */
public class IFCFactoryTest {
    
    public IFCFactoryTest() {
    }

    /**
     * Test of parseIfc method, of class IFCFactory.
     * EntityCollection parseIfc (String)
     * Turns ifc file content into entityCollection
     */
    @Test
    public void testParseIfc() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("parseIfc");
        
        String file = "DATA;\r\n#23=ifcroof();\nENDSEC;";
        
        IFCFactory factory = new IFCFactory();
        EntityCollection result = factory.parseIfc(file);
        
        assertEquals("roof23", result.roofs.get(23));
        assertEquals(0, result.buildings.size());
    }

    /**
     * Test of extractData method, of class IFCFactory.
     * String extractData (String)
     * removes everything before and after the DATA section
     * of the IFC file
     */
    @Test
    public void testExtractData() {
        System.out.println("extractData");
        String file = "ISO HEADER;...ignore Syntax...ENDSEC; DATA;::EXTRACT THIS::ENDSEC; ISO;";
        IFCFactory instance = new IFCFactory();
        
        String expResult = "::EXTRACT THIS::";
        String result = instance.extractData(file);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeLineBreaks method, of class IFCFactory.
     * String removeLineBreaks (String)
     * removes line breaks
     */
    @Test
    public void testRemoveLineBreaks() {
        System.out.println("removeLineBreaks");
        String input = "\n \r\n a . \n* '\r\n' \\(\n\\)";
        IFCFactory instance = new IFCFactory();
        String expResult = "  a . * '' \\(\\)";
        String result = instance.removeLineBreaks(input);
        assertEquals(expResult, result);
        
    }

    
    /**
     * Test of findId.
     * int removeLineBreaks (String)
     * finds the entity id in an ifc line
     * #21 = IFCTEST(); would return 21
     */
    @Test
    public void testFindId() {
        System.out.println("findId");
        String[] inputs = {"#21=ifctest();", "#1 =ifctest();", "# 54521=ifctest( );", " # 894321564 =ifctest() ;"};
        int[] expResults = {21, 1, 54521, 894321564};
        
        IFCFactory instance = new IFCFactory();
        //String result = instance.removeLineBreaks(input);
        for (int i = 0; i < inputs.length; i++) {
            assertEquals(expResults[i], instance.findId(inputs[i]));
        }
    }
    
    
     /**
     * Test of findDefinition.
     * int removeLineBreaks (String)
     * finds the entity id in an ifc line
     * #21 = IFCTEST(); would return 21
     */
    @Test
    public void testFindDefinition() {
        System.out.println("findDefinition");
        String[] inputs = {"#21=ifcRelAggregates();", "#1 =IFCroom();", "# 54521=IFCBUILDING( );", " # 89432156486 =ifcbuildingstorey() ;"};
        String[] expResults = {"ifcrelaggregates", "ifcroom", "ifcbuilding", "ifcbuildingstorey"};
        
        IFCFactory instance = new IFCFactory();
        //String result = instance.removeLineBreaks(input);
        for (int i = 0; i < inputs.length; i++) {
            assertEquals(expResults[i], instance.findDefinition(inputs[i]));
        }
    }    
    
    
    /**
     * Test of splitData method, of class IFCFactory.
     * String[] splitData (String)
     * Splits ifc data section by regex ); line endings
     */
    @Test
    public void testSplitData() {
        System.out.println("splitData");
        String data = "ifc();ifc(); ifc(  ); ifc() \n; ifc ();";
        // \n only tested for syntactical reasons
        // all line breaks are removed by another method
        
        IFCFactory instance = new IFCFactory();
        String[] result = instance.splitData(data);
        
        assertEquals(result.length, 5);
        
        for (int i = 0; i < result.length; i++) {
            //System.out.println(i + ": " + result[i]);
            result[i] = result[i].replaceAll("\\s*", "");
            assertEquals("ifc(", result[i]);
        }
    }

    /**
     * Test of interpretLines method, of class IFCFactory.
     * EntityCollection interpretLines (String[])
     * populates entityCollection with content of ifc file lines
     */
    @Test
    public void testInterpretLines() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("interpretLines");
        String[] lines = {"#1=ifcroof()", "#5451=ifcbuilding();", "#48691=ifczone();"};
        
        IFCFactory factory = new IFCFactory();
        EntityCollection entityCollection = factory.interpretLines(lines, new EntityCollection());
        
        assertEquals(entityCollection.roofs.get(1), "roof1");
        assertEquals(entityCollection.buildings.get(5451), "building5451");
        assertEquals(entityCollection.zones.get(48691), "zone48691");
    }

    /**
     * Test of createRoomZoneLookup method, of class IFCFactory.
     * EntityCollection createRoomZoneLookup (EntityCollection)
     * reverse mapping of rooms and zones so that 
     * zones can be found by looking up rooms
     * instead of constant traversal by looking for rooms within all zones.
     */
    @Test
    public void testCreateRoomZoneLookup() {
        System.out.println("createRoomZoneLookup");
        
        EntityCollection entityCollection = new EntityCollection();
        entityCollection.zones.put(1, "zone1");
        entityCollection.rooms.put(4, "room4");
        entityCollection.rooms.put(6, "room6");
        ArrayList<Integer> roomsInZone = new ArrayList();
        roomsInZone.add(4);
        roomsInZone.add(6);
        entityCollection.relAssignsToGroups.put(1, roomsInZone);
        
        IFCFactory factory = new IFCFactory();
        entityCollection = factory.createRoomZoneLookup(entityCollection);
        
        System.out.println(entityCollection.roomsInZones.size());
        System.out.println(entityCollection.roomsInZones.keySet());
        
        int zoneA = entityCollection.roomsInZones.get(4);
        int zoneB = entityCollection.roomsInZones.get(6);
        
        assertEquals(zoneA, 1);
        assertEquals(zoneB, 1);
    }
    
}
