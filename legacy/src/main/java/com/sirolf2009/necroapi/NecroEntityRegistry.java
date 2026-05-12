package com.sirolf2009.necroapi;

import java.util.HashMap;
import java.util.Map;

public class NecroEntityRegistry
{
    public static final Map<String, NecroEntityBase> registeredEntities = new HashMap<>();

    public static void registerEntity(NecroEntityBase entity)
    {
        registeredEntities.put(entity.mobName, entity);
    }
}
