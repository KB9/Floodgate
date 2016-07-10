package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.Game;

public class Floodgate extends Game {

    @Override
    public void create() {
        setScreen(new LevelSelectorScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {

    }
}
