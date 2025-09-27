package edu.eci.arsw.blueprints.test.persistence.impl;

import edu.eci.arsw.blueprints.services.BlueprintsServices;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.services.RedundancyBlueprintFilter;
import edu.eci.arsw.blueprints.services.SubsamplingBlueprintFilter;
import edu.eci.arsw.blueprints.persistence.impl.InMemoryBlueprintPersistence;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlueprintFilterTest {
    @Test
    public void testRedundancyFilter() throws Exception {
        RedundancyBlueprintFilter filter = new RedundancyBlueprintFilter();
        Point[] pts = new Point[]{new Point(1,1), new Point(1,1), new Point(2,2), new Point(2,2), new Point(3,3)};
        Blueprint bp = new Blueprint("author", "redundant", pts);
        bp.setPoints(filter.filter(bp.getPoints()));
        assertEquals(3, bp.getPoints().size());
        assertEquals(new Point(1,1), bp.getPoints().get(0));
        assertEquals(new Point(2,2), bp.getPoints().get(1));
        assertEquals(new Point(3,3), bp.getPoints().get(2));
    }

    @Test
    public void testSubsamplingFilter() throws Exception {
        SubsamplingBlueprintFilter filter = new SubsamplingBlueprintFilter();
        Point[] pts = new Point[]{new Point(1,1), new Point(2,2), new Point(3,3), new Point(4,4)};
        Blueprint bp = new Blueprint("author", "subsample", pts);
        bp.setPoints(filter.filter(bp.getPoints()));
        assertEquals(2, bp.getPoints().size());
        assertEquals(new Point(1,1), bp.getPoints().get(0));
        assertEquals(new Point(3,3), bp.getPoints().get(1));
    }

        @Test
    public void testBlueprintsServicesWithRedundancyFilter() throws BlueprintPersistenceException, BlueprintNotFoundException {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        RedundancyBlueprintFilter filter = new RedundancyBlueprintFilter();
        BlueprintsServices services = new BlueprintsServices(persistence, filter);

        Blueprint bp = new Blueprint("author", "redundant", new Point[]{new Point(1,1), new Point(1,1), new Point(2,2)});
        services.addNewBlueprint(bp);
        Blueprint result = services.getBlueprint("author", "redundant");
        assertEquals(2, result.getPoints().size());
        assertEquals(new Point(1,1), result.getPoints().get(0));
        assertEquals(new Point(2,2), result.getPoints().get(1));
    }

    @Test
    public void testBlueprintsServicesWithSubsamplingFilter() throws BlueprintPersistenceException, BlueprintNotFoundException {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        SubsamplingBlueprintFilter filter = new SubsamplingBlueprintFilter();
        BlueprintsServices services = new BlueprintsServices(persistence, filter);

        Blueprint bp = new Blueprint("author", "subsample", new Point[]{new Point(1,1), new Point(2,2), new Point(3,3), new Point(4,4)});
        services.addNewBlueprint(bp);
        Blueprint result = services.getBlueprint("author", "subsample");
        assertEquals(2, result.getPoints().size());
        assertEquals(new Point(1,1), result.getPoints().get(0));
        assertEquals(new Point(3,3), result.getPoints().get(1));
    }
}
