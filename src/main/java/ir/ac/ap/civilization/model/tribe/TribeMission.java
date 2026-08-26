package ir.ac.ap.civilization.model.tribe;

import ir.ac.ap.civilization.model.resource.ResourceType;
import java.io.Serializable;

public final class TribeMission implements Serializable {
    private final String title;
    private final ResourceType requiredResource;
    private final int requiredAmount;
    private final int relationReward;
    private int remainingTurns;
    private MissionStatus status = MissionStatus.OFFERED;

    public TribeMission(String title, ResourceType requiredResource,
                        int requiredAmount, int remainingTurns, int relationReward) {
        this.title = title;
        this.requiredResource = requiredResource;
        this.requiredAmount = requiredAmount;
        this.remainingTurns = remainingTurns;
        this.relationReward = relationReward;
    }
    public String getTitle() { return title; }
    public ResourceType getRequiredResource() { return requiredResource; }
    public int getRequiredAmount() { return requiredAmount; }
    public int getRemainingTurns() { return remainingTurns; }
    public int getRelationReward() { return relationReward; }
    public MissionStatus getStatus() { return status; }
    public void activate() { if (status == MissionStatus.OFFERED) status = MissionStatus.ACTIVE; }
    public void complete() { status = MissionStatus.COMPLETED; }
    public void cancel() { status = MissionStatus.CANCELLED; }
    public void advanceTurn() {
        if (status != MissionStatus.ACTIVE) return;
        remainingTurns--;
        if (remainingTurns <= 0) status = MissionStatus.FAILED;
    }
}
