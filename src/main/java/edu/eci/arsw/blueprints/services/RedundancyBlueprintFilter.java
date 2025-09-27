package edu.eci.arsw.blueprints.services;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("redundancyFilter")
public class RedundancyBlueprintFilter implements BlueprintFilter {
    @Override
    public List<Point> filter(List<Point> points) {
        List<Point> filtered = new ArrayList<>();
        Point prev = null;
        for (Point p : points) {
            if (prev == null || !p.equals(prev)) {
                filtered.add(p);
            }
            prev = p;
        }
        return filtered;
    }
    
    @Override
    public Blueprint filterBlueprint(Blueprint bp) {
        List<Point> originalPoints = bp.getPoints();
        List<Point> filteredPoints = new ArrayList<>();

        if (!originalPoints.isEmpty()) {
            filteredPoints.add(originalPoints.get(0));
        }

        for (int i = 1; i < originalPoints.size(); i++) {
            if (!originalPoints.get(i).equals(originalPoints.get(i - 1))) {
                filteredPoints.add(originalPoints.get(i));
            }
        }

        // Crear una copia del blueprint para evitar modificar el original
        return new Blueprint(bp.getAuthor(), bp.getName(), filteredPoints.toArray(new Point[0]));
    }

    @Override
    public Set<Blueprint> filterBlueprints(Set<Blueprint> blueprints) {
        Set<Blueprint> filteredBlueprints = new HashSet<>();
        for (Blueprint bp : blueprints) {
            filteredBlueprints.add(filterBlueprint(bp));
        }
        return filteredBlueprints;
    }
}
