package com.savemygpa.player;

import com.savemygpa.player.effect.StatusEffect;
import java.util.*;

public class Player {

    private int energy;
    private int intelligence;
    private int mood;

    private static final int BASE_MAX_ENERGY       = 10;
    private static final int BASE_MAX_INTELLIGENCE = 100;
    private static final int BASE_MAX_MOOD         = 100;

    private List<StatusEffect> effects = new ArrayList<>();

    public Player(int energy, int intelligence, int mood) {
        this.energy       = energy;
        this.intelligence = intelligence;
        this.mood         = mood;
    }

    public void addEffect(StatusEffect effect) {
        if (hasEffect(effect.getClass())) return;
        effects.add(effect);
        effect.onApply(this);
    }

    public void updateEffectsOnTransition() {
        Iterator<StatusEffect> it = effects.iterator();
        while (it.hasNext()) {
            StatusEffect e = it.next();
            e.onTransition(this);
            if (e.isExpired()) {
                e.onExpire(this);
                it.remove();
            }
        }
    }

    /**
     * Force-removes an effect by class — safe to call even if not active.
     * Used by: GameLauncher.clearDayEffects() for WetFeet + NoStackOverflow,
     *          ClassroomActivity for WhyDizzy.
     */
    public <T extends StatusEffect> void removeEffect(Class<T> effectClass) {
        Iterator<StatusEffect> it = effects.iterator();
        while (it.hasNext()) {
            StatusEffect e = it.next();
            if (effectClass.isInstance(e)) {
                e.onExpire(this);
                it.remove();
                return;
            }
        }
    }

    public boolean hasEffect(Class<? extends StatusEffect> effectClass) {
        return effects.stream().anyMatch(effectClass::isInstance);
    }

    @SuppressWarnings("unchecked")
    public <T extends StatusEffect> Optional<T> getEffect(Class<T> effectClass) {
        return effects.stream()
                .filter(effectClass::isInstance)
                .map(e -> (T) e)
                .findFirst();
    }

    public List<StatusEffect> getActiveEffects() {
        return Collections.unmodifiableList(effects);
    }

    public int getStat(StatType type) {
        return switch (type) {
            case ENERGY       -> energy;
            case INTELLIGENCE -> intelligence;
            case MOOD         -> mood;
        };
    }

    public void changeStat(StatType type, int amount) {
        switch (type) {
            case ENERGY -> {
                int cap = getEffectiveMax(StatType.ENERGY);
                energy = clamp(energy + amount, 0, cap);
            }
            case INTELLIGENCE ->
                    intelligence = clamp(intelligence + amount, 0, BASE_MAX_INTELLIGENCE);
            case MOOD -> {
                int cap = getEffectiveMax(StatType.MOOD);
                mood = clamp(mood + amount, 0, cap);
            }
        }
    }

    public void changeIntelligenceFromEffect(int baseGain) {
        int modified = baseGain;
        for (StatusEffect e : effects) modified = e.modifyIntelligenceGain(modified);
        changeStat(StatType.INTELLIGENCE, modified);
    }

    private int getEffectiveMax(StatType type) {
        int cap = switch (type) {
            case ENERGY       -> BASE_MAX_ENERGY;
            case INTELLIGENCE -> BASE_MAX_INTELLIGENCE;
            case MOOD         -> BASE_MAX_MOOD;
        };
        for (StatusEffect e : effects) cap = e.modifyStatCap(type, cap);
        return cap;
    }

    public StatTier getStatTier(int amount) {
        if (amount >= 70) return StatTier.HIGH;
        if (amount >= 31) return StatTier.MEDIUM;
        return StatTier.LOW;
    }

    public boolean hasStat(StatType type, int require) {
        return getStat(type) >= require;
    }

    public double getEventChanceMultiplier() {
        double m = 1.0;
        for (StatusEffect e : effects) m *= e.getEventChanceMultiplier();
        return m;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}