package upgradeableevents.interfaces;

public interface UpgradeableEvent {
    void applyUpgrade();
    boolean canBeUpgraded();
}