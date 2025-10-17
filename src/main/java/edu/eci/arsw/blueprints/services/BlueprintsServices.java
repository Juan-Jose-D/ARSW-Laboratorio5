    
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blueprints.services;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintsPersistence;
// ...existing code...
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hcadavid
 */
@Service
public class BlueprintsServices {

    private final BlueprintsPersistence bpp;
    private final BlueprintFilter blueprintFilter;

    @Autowired
    public BlueprintsServices(BlueprintsPersistence bpp,
            @org.springframework.beans.factory.annotation.Qualifier("redundancyFilter") BlueprintFilter blueprintFilter) {
        this.bpp = bpp;
        this.blueprintFilter = blueprintFilter;
    }

    public void addNewBlueprint(Blueprint bp) throws edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException {
        bpp.saveBlueprint(bp);
    }

    public Set<Blueprint> getAllBlueprints() throws BlueprintNotFoundException{
        return blueprintFilter.filterBlueprints(bpp.getAllBluePrints());
    }

    /**
     * 
     * @param author blueprint's author
     * @param name   blueprint's name
     * @return the blueprint of the given name created by the given author
     * @throws BlueprintNotFoundException if there is no such blueprint
     */
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        Blueprint bp = bpp.getBlueprint(author, name);
        if (bp == null)
            throw new BlueprintNotFoundException("Blueprint not found");
        
        // Crear una copia del blueprint para evitar modificar el original
        Blueprint filteredBp = new Blueprint(bp.getAuthor(), bp.getName(), bp.getPoints().toArray(new Point[0]));
        filteredBp.setPoints(blueprintFilter.filter(filteredBp.getPoints()));
        return filteredBp;
    }

    /**
     * 
     * @param author blueprint's author
     * @return all the blueprints of the given author
     * @throws BlueprintNotFoundException if the given author doesn't exist
     */
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        return blueprintFilter.filterBlueprints(bpp.getBlueprintsByAuthor(author));
    }


    public void updateBlueprint(String author, String bpname, Blueprint updatedBlueprint) throws BlueprintNotFoundException {
        bpp.updateBlueprint(author, bpname, updatedBlueprint);
    }

    public void deleteBlueprint(String author, String bpname) throws BlueprintNotFoundException {
        bpp.deleteBlueprint(author, bpname);
    }


    public Set<String> getAllAuthors() {
        Set<Blueprint> allBlueprints;
        try {
            allBlueprints = bpp.getAllBluePrints();
        } catch (BlueprintNotFoundException e) {
            return java.util.Collections.emptySet();
        }
        return allBlueprints.stream().map(Blueprint::getAuthor).collect(java.util.stream.Collectors.toSet());
    }

}
