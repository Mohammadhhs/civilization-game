package ir.ac.ap.civilization.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexMapResourceDepositTest {

    @Test
    void sameSeedShouldCreateSameDepositAmounts() {
        HexMapGenerator generator = new HexMapGenerator();
        GameMap firstMap = generator.generate(7, 987654L);
        GameMap secondMap = generator.generate(7, 987654L);

        for (HexTile firstTile : firstMap.getTiles()) {
            HexTile secondTile = secondMap
                    .getTile(firstTile.getCoordinate())
                    .orElseThrow(AssertionError::new);

            for (NaturalResourceType resourceType :
                    firstTile.getNaturalResources()) {

                assertEquals(
                        firstTile.getInitialResourceAmount(
                                resourceType
                        ),
                        secondTile.getInitialResourceAmount(
                                resourceType
                        )
                );
            }
        }
    }

    @Test
    void generatedDepositsShouldStartPositiveAndFull() {
        GameMap gameMap =
                new HexMapGenerator().generate(7, 222L);

        for (HexTile tile : gameMap.getTiles()) {
            for (NaturalResourceType resourceType :
                    tile.getNaturalResources()) {

                int initialAmount =
                        tile.getInitialResourceAmount(resourceType);

                assertTrue(initialAmount > 0);
                assertEquals(
                        initialAmount,
                        tile.getRemainingResourceAmount(
                                resourceType
                        )
                );
            }
        }
    }
}
