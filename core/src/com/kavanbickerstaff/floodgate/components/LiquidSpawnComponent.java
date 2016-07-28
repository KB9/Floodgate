package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;

public class LiquidSpawnComponent implements Component {

    public boolean spawnOnNextUpdate;
    public long spawnTimeMillis;
    public boolean spawnContinuous;

    public boolean isFinite;
    public int spawnCount;

    public float spawnVelocityX, spawnVelocityY;

    public boolean on;
}
