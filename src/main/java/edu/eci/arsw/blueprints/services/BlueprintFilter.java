package edu.eci.arsw.blueprints.services;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import java.util.List;
import java.util.Set;

public interface BlueprintFilter {
    List<Point> filter(List<Point> points);

    /**
     * Filtrar un solo plano
     * @param bp Plano a filtrar
     * @return Plano filtrado
     */
    public Blueprint filterBlueprint(Blueprint bp);

    /**
     * Filtrar un conjunto de planos
     * @param blueprints Conjunto de planos a filtrar
     * @return Conjunto filtrado
     */
    public Set<Blueprint> filterBlueprints(Set<Blueprint> blueprints);
}
