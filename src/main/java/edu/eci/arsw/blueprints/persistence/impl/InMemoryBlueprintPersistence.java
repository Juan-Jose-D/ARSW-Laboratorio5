/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blueprints.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.BlueprintsPersistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author hcadavid
 */
import org.springframework.stereotype.Component;

@Component
public class InMemoryBlueprintPersistence implements BlueprintsPersistence{

    private final Map<Tuple<String,String>,Blueprint> blueprints=new ConcurrentHashMap<>();

    public InMemoryBlueprintPersistence() {
        // Casa
    Point[] casa = new Point[]{
        new Point(100, 300), // base izquierda
        new Point(300, 300), // base derecha
        new Point(300, 200), // techo derecha
        new Point(200, 100), // pico del techo
        new Point(100, 200), // techo izquierda
        new Point(100, 300)  // cierra la base
    };

    // Estrella de 5 puntas
    Point[] estrella = new Point[]{
        new Point(200, 120),
        new Point(218, 180),
        new Point(280, 180),
        new Point(230, 220),
        new Point(250, 280),
        new Point(200, 240),
        new Point(150, 280),
        new Point(170, 220),
        new Point(120, 180),
        new Point(182, 180),
        new Point(200, 120)
    };

    // Triángulo
    Point[] triangulo = new Point[]{
        new Point(120, 300),
        new Point(280, 300),
        new Point(200, 150),
        new Point(120, 300)
    };

    // Cuadrado
    Point[] cuadrado = new Point[]{
        new Point(120, 120),
        new Point(280, 120),
        new Point(280, 280),
        new Point(120, 280),
        new Point(120, 120)
    };

    // Círculo aproximado (polígono de 12 lados)
    Point[] circulo = new Point[]{
        new Point(200, 120),
        new Point(243, 133),
        new Point(273, 167),
        new Point(280, 210),
        new Point(266, 250),
        new Point(233, 277),
        new Point(200, 280),
        new Point(167, 277),
        new Point(134, 250),
        new Point(120, 210),
        new Point(127, 167),
        new Point(157, 133),
        new Point(200, 120)
    };

    Blueprint bp1 = new Blueprint("juan", "casa", casa);
    Blueprint bp2 = new Blueprint("juan", "estrella", estrella);
    Blueprint bp3 = new Blueprint("maria", "triangulo", triangulo);
    Blueprint bp4 = new Blueprint("carlos", "cuadrado", cuadrado);
    Blueprint bp5 = new Blueprint("ana", "circulo", circulo);

        blueprints.put(new Tuple<>(bp1.getAuthor(),bp1.getName()), bp1);
        blueprints.put(new Tuple<>(bp2.getAuthor(),bp2.getName()), bp2);
        blueprints.put(new Tuple<>(bp3.getAuthor(),bp3.getName()), bp3);
        blueprints.put(new Tuple<>(bp4.getAuthor(),bp4.getName()), bp4);
        blueprints.put(new Tuple<>(bp5.getAuthor(),bp5.getName()), bp5);

    }
    
    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (blueprints.putIfAbsent(new Tuple<>(bp.getAuthor(),bp.getName()), bp) != null){
            throw new BlueprintPersistenceException("The given blueprint already exists: "+bp);
        }     
    }

    @Override
    public Blueprint getBlueprint(String author, String blueprintName) throws BlueprintNotFoundException {
        Tuple<String, String> blueprintKey = new Tuple<>(author, blueprintName);
        Blueprint bp = blueprints.get(blueprintKey);
        if (bp == null) {
            throw new BlueprintNotFoundException("Blueprint not found: " + author + ", " + blueprintName);
        }
        return bp;
    }
    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<Blueprint> authorBlueprints = blueprints.values().stream()
                .filter(bp -> bp.getAuthor().equals(author))
                .collect(Collectors.toSet());

        if (authorBlueprints.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints found for author: " + author);
        }

        return authorBlueprints;
    }

    @Override
    public void updateBlueprint (String author, String bpname, Blueprint updatedBlueprint)throws BlueprintNotFoundException{
        Tuple<String, String> key = new Tuple<>(author, bpname);
        // Construir una nueva instancia para evitar mutación in-place
        Blueprint replacement = new Blueprint(author, bpname,
                updatedBlueprint.getPoints().toArray(new Point[0]));

        Blueprint result = blueprints.computeIfPresent(key, (k, existing) -> replacement);
        if (result == null) {
            // Si no existía el plano, computeIfPresent no ejecuta el mapeo
            throw new BlueprintNotFoundException(BlueprintNotFoundException.NONEXISTENT);
        }
    }


    @Override
    public void deleteBlueprint(String author, String bpname) throws BlueprintNotFoundException {
        Tuple<String, String> key = new Tuple<>(author, bpname);
        Blueprint removed = blueprints.remove(key);
        if (removed == null) {
            throw new BlueprintNotFoundException("Blueprint not found: " + author + ", " + bpname);
        }
    }

    @Override
    public Set<Blueprint> getAllBluePrints() {
        return blueprints.values().stream().collect(Collectors.toSet());
    }
}
