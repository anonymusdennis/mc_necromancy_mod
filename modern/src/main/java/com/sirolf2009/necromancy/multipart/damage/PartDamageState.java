package com.sirolf2009.necromancy.multipart.damage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Per-part durability and gameplay flags; aggregated into {@link MultipartHealthAggregate} on the root mob.
 */
public final class PartDamageState {

    private float maxHealth = 1f;
    private float currentHealth = 1f;
    private boolean severed;
    private boolean destroyed;
    private final EnumSet<PartFunctionalFlag> flags = EnumSet.noneOf(PartFunctionalFlag.class);
    private final List<PartArmorLayer> armorLayers = new ArrayList<>();
    private CriticalZone criticalZone = CriticalZone.GENERIC;

    public float maxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = Math.max(1e-4f, maxHealth);
        this.currentHealth = Math.min(currentHealth, this.maxHealth);
    }

    public float currentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float currentHealth) {
        this.currentHealth = Math.max(0f, Math.min(maxHealth, currentHealth));
    }

    public boolean severed() {
        return severed;
    }

    public void setSevered(boolean severed) {
        this.severed = severed;
        if (severed) {
            flags.add(PartFunctionalFlag.DETACHED);
        }
    }

    public boolean destroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
        if (destroyed) {
            flags.add(PartFunctionalFlag.DISABLED);
        }
    }

    public EnumSet<PartFunctionalFlag> flagsView() {
        if (flags.isEmpty()) {
            return EnumSet.noneOf(PartFunctionalFlag.class);
        }
        return EnumSet.copyOf(flags);
    }

    public void addFlag(PartFunctionalFlag f) {
        flags.add(f);
    }

    public void removeFlag(PartFunctionalFlag f) {
        flags.remove(f);
    }

    public boolean hasFlag(PartFunctionalFlag f) {
        return flags.contains(f);
    }

    public List<PartArmorLayer> armorLayersView() {
        return List.copyOf(armorLayers);
    }

    public void clearArmorLayers() {
        armorLayers.clear();
    }

    public void addArmorLayer(PartArmorLayer layer) {
        armorLayers.add(layer);
    }

    public CriticalZone criticalZone() {
        return criticalZone;
    }

    public void setCriticalZone(CriticalZone criticalZone) {
        this.criticalZone = criticalZone == null ? CriticalZone.GENERIC : criticalZone;
    }

    public void copyFrom(PartDamageState o) {
        maxHealth = o.maxHealth;
        currentHealth = o.currentHealth;
        severed = o.severed;
        destroyed = o.destroyed;
        flags.clear();
        flags.addAll(o.flags);
        armorLayers.clear();
        armorLayers.addAll(o.armorLayers);
        criticalZone = o.criticalZone;
    }

    public PartDamageState copy() {
        PartDamageState p = new PartDamageState();
        p.copyFrom(this);
        return p;
    }
}
