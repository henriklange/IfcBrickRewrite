/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rewrite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author henriklange
 */
public class IFCEntityFactory {
    
    private ArrayList<String> notExists = new ArrayList();
    
    public EntityCollection dispatch(int id, String definition, String line, EntityCollection entityCollection) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        if(!notExists.contains(definition)){
            try{
                Method method = this.getClass().getMethod(definition, int.class, String.class, EntityCollection.class);
                entityCollection = (EntityCollection) method.invoke(this, id, line, entityCollection);
                return entityCollection;
            } catch (NoSuchMethodException ex) {
                notExists.add(definition);
                return entityCollection;
            }
        }
        return entityCollection;
    }
    
    
    public EntityCollection ifcbuilding(int id, String line, EntityCollection entityCollection){
        entityCollection.buildings.put(id, "building" + id);
        return entityCollection;
    }
    
    public EntityCollection ifcroof(int id, String line, EntityCollection entityCollection){
        entityCollection.roofs.put(id, "roof" + id);
        return entityCollection;
    }
    
    public EntityCollection ifcgroup(int id, String line, EntityCollection entityCollection){
        entityCollection.zones.put(id, "zone" + id);
        return entityCollection;
    }
    
    public EntityCollection ifczone(int id, String line, EntityCollection entityCollection){
        entityCollection.zones.put(id, "zone" + id);
        return entityCollection;
    }
    
    public EntityCollection ifcbuildingstorey(int id, String line, EntityCollection entityCollection){
        // #id= ifcbuildingstorey('string',$,'STRING' ...);
        entityCollection.floors.put(id, line
                                    .split("'")[3]
                                    .replace(" ", "_")
                                    .replace(":", "_"));
        return entityCollection;
    }
    
    public EntityCollection ifcspace(int id, String line, EntityCollection entityCollection){
        //#id= ifcspace('string',#id,'1',$,$,#id,#id,'NAME',.ELEMENT.,.INTERNAL.,$);
        //#134= IFCSPACE('0CRPz_SEr94Ah74P8LhwLP',#61,'R301','N/A',$,#121,#132,'Roof',.ELEMENT.,.INTERNAL.,$);
        //see difference in examples ( 'N/A' ) string
        //we need second and last string

        //this is another version, were the last two strings make up a single string, since they are within the same set of commas
        //#7036=IFCSPACE('1ApixdShTADgzYQQNPEpeH',#1,'1D40',$,$,#5962,#278487,'IMMUNIZ''N ROOM',.ELEMENT.,.INTERNAL.,$);
        //this could be found with something line split(",'")[1].split("',")[0] for one part and split()


        Pattern pattern = Pattern.compile(",'(.*?)',");
        Matcher matcher = pattern.matcher(line);

        String name = "";

        while(matcher.find()){
            if(name.equals("")){
                name = matcher.group(1) + name;
            } else {
                name = matcher.group(1) + 
                        // "_" +
                        name;
            }
        }

        entityCollection.rooms.put(id, name
                                .replace("''", "_")
                                .replace("' '", "_")
                                .replace(" ", "_")
                                .replace(":", "_"));
        return entityCollection;
    }
    
    
    
    
    
    public EntityCollection ifcrelaggregates(int id, String line, EntityCollection entityCollection){
       
        //#id= ifcrelaggrecates('string', #id,$,$,#ID,(ENTITYCOLLECTION)...);
        
        ArrayList<Integer> entities = new ArrayList();
        String contentCollection = line
                .split("\\(")[2];
        contentCollection = contentCollection.substring(0, contentCollection.indexOf(")"));

        for(String eid : contentCollection.split(",")){
            entities.add(Integer.parseInt(eid.replace("#", "")));
        }

        entityCollection.relaggregates.put(Integer.parseInt(line.split(",")[4].replace("#", "")), entities);

        
        return entityCollection;
    }
    
    
    
    public EntityCollection ifcrelassignstogroup(int id, String line, EntityCollection entityCollection){
        
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

        
        return entityCollection;
    }
    
}
