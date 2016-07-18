package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;

public class LiquidSpawnComponent implements Component {

    public long spawnTimeMillis;
    public boolean spawnOnNextUpdate;
    public boolean spawnContinuous;

    public float spawnVelocityX, spawnVelocityY;

    public boolean on;
}
