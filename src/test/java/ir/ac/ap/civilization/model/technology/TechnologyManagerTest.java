package ir.ac.ap.civilization.model.technology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnologyManagerTest {

    @Test
    void stoneMiningShouldBeAvailableInitially() {
        TechnologyManager manager =
                new TechnologyManager();

        assertTrue(
                manager.canResearch(
                        TechnologyType.STONE_MINING
                )
        );
    }

    @Test
    void ironMiningShouldRequireStoneMining() {
        TechnologyManager manager =
                new TechnologyManager();

        assertFalse(
                manager.canResearch(
                        TechnologyType.IRON_MINING
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> manager.completeResearch(
                        TechnologyType.IRON_MINING
                )
        );

        manager.completeResearch(
                TechnologyType.STONE_MINING
        );

        assertTrue(
                manager.canResearch(
                        TechnologyType.IRON_MINING
                )
        );
    }

    @Test
    void professionalToolsShouldRequireBothMiningTechnologies() {
        TechnologyManager manager =
                new TechnologyManager();

        manager.completeResearch(
                TechnologyType.STONE_MINING
        );

        assertFalse(
                manager.canResearch(
                        TechnologyType.PROFESSIONAL_TOOLS
                )
        );

        manager.completeResearch(
                TechnologyType.IRON_MINING
        );

        assertTrue(
                manager.canResearch(
                        TechnologyType.PROFESSIONAL_TOOLS
                )
        );
    }

    @Test
    void storageUpgradeTwoShouldRequireFirstUpgrade() {
        TechnologyManager manager =
                new TechnologyManager();

        assertFalse(
                manager.canResearch(
                        TechnologyType.STORAGE_UPGRADE_II
                )
        );

        manager.completeResearch(
                TechnologyType.STORAGE_UPGRADE_I
        );

        assertTrue(
                manager.canResearch(
                        TechnologyType.STORAGE_UPGRADE_II
                )
        );
    }

    @Test
    void completedTechnologyShouldNotBeResearchableAgain() {
        TechnologyManager manager =
                new TechnologyManager();

        manager.completeResearch(
                TechnologyType.STONE_MINING
        );

        assertFalse(
                manager.canResearch(
                        TechnologyType.STONE_MINING
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> manager.completeResearch(
                        TechnologyType.STONE_MINING
                )
        );
    }

    @Test
    void professionalToolsShouldApplyOnePointFiveMultiplier() {
        TechnologyManager manager =
                new TechnologyManager();

        assertEquals(
                1.0,
                manager.getMiningProductionMultiplier()
        );

        manager.completeResearch(
                TechnologyType.STONE_MINING
        );

        manager.completeResearch(
                TechnologyType.IRON_MINING
        );

        manager.completeResearch(
                TechnologyType.PROFESSIONAL_TOOLS
        );

        assertEquals(
                1.5,
                manager.getMiningProductionMultiplier()
        );
    }
}