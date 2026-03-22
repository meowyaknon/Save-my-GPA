package com.savemygpa.player.effect.debuff;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import com.savemygpa.player.effect.StatusEffect;

public class WetFeetDebuff extends StatusEffect {

    public WetFeetDebuff() {
        super(99);
    }

    @Override
    public void onApply(Player player) {
        System.out.println("[Debuff] Wet Feet — your socks are soaked. Energy drips per step.");
    }

    @Override
    public void onExpire(Player player) {
        System.out.println("[Debuff] Wet Feet cleared — finally dry.");
    }

    @Override
    public void onTransition(Player player) {
        super.onTransition(player);
        player.changeStat(StatType.ENERGY, -1);
    }

    @Override
    public String getName() {
        return "Wet Feet";
    }
}