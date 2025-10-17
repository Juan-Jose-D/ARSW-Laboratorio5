    
package edu.eci.arsw.blueprints.controllers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author hcadavid
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/blueprints")
public class BlueprintsAPIController {
    @Autowired
    private BlueprintsServices blueprintsServices;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> manejadorGetBlueprints() {
        try {
            Set<Blueprint> blueprints = blueprintsServices.getAllBlueprints();
            return new ResponseEntity<>(blueprints, HttpStatus.ACCEPTED);
        } catch (BlueprintNotFoundException e) {
            Logger.getLogger(BlueprintsAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("Error al obtener los planos", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> manejadorPostBlueprint(@RequestBody Blueprint blueprint) {
        try {
            blueprintsServices.addNewBlueprint(blueprint);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (BlueprintPersistenceException e) {
            Logger.getLogger(BlueprintsAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("Error al registrar el plano.", HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/{author}", method = RequestMethod.GET)
    public ResponseEntity<?> getBlueprintsByAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> blueprints = blueprintsServices.getBlueprintsByAuthor(author);
            return new ResponseEntity<>(blueprints, HttpStatus.ACCEPTED);
        } catch (BlueprintNotFoundException e) {
            Logger.getLogger(BlueprintsAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("Autor no encontrado: " + author, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/authors", method = RequestMethod.GET)
    public ResponseEntity<Set<String>> getAuthors() {
        System.out.println("[DEBUG] GET /blueprints/authors called");
        Set<String> authors = blueprintsServices.getAllAuthors();
        System.out.println("[DEBUG] Authors found: " + authors);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @RequestMapping(value = "/{author}/{bpname}", method = RequestMethod.GET)
    public ResponseEntity<?> getBlueprintsByAuthorAndBpname(@PathVariable String author,
            @PathVariable String bpname) {
        try {
            Blueprint blueprint = blueprintsServices.getBlueprint(author, bpname);
            return new ResponseEntity<>(blueprint, HttpStatus.ACCEPTED);
        } catch (BlueprintNotFoundException e) {
            Logger.getLogger(BlueprintsAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("Plano no encontrado: " + author + "/" + bpname, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{author}/{bpname}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateBlueprint(
            @PathVariable String author,
            @PathVariable String bpname,
            @RequestBody Blueprint updatedBlueprint) {
        try {
            blueprintsServices.updateBlueprint(author, bpname, updatedBlueprint);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (BlueprintNotFoundException e) {
            Logger.getLogger(BlueprintsAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("Plano no encontrado: " + author + "/" + bpname, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{author}/{bpname}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteBlueprint(
            @PathVariable String author,
            @PathVariable String bpname) {
        try {
            blueprintsServices.deleteBlueprint(author, bpname);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (BlueprintNotFoundException e) {
            Logger.getLogger(BlueprintsAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>("Plano no encontrado: " + author + "/" + bpname, HttpStatus.NOT_FOUND);
        }
    }
}