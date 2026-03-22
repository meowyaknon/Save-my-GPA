package com.savemygpa.player.effect.buff;

import com.savemygpa.player.Player;
import com.savemygpa.player.effect.StatusEffect;

public class IndefatigableBuff extends StatusEffect {

    public IndefatigableBuff() {
        super(1);
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Buff] Indefatigable — the chicken gives you strength. No fatigue next activity!");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Buff] Indefatigable faded.");
    }

    @Override
    public String getName() {
        return "Indefatigable";
    }
}