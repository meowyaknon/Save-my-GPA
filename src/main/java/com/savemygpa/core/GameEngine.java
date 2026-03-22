package com.savemygpa.core;

public class GameEngine {

    private GameState currentState = GameState.MAIN_MENU;

    public GameState getCurrentState() {
        return currentState;
    }

    public void setState(GameState state) {
        System.out.println("[GameEngine] State: " + currentState + " → " + state);
        this.currentState = state;
    }

    public boolean isInState(GameState state) {
        return currentState == state;
    }
}