package de.mirkosertic.gameengine.sprites;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.GameComponent;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.ResourceName;

public class SpriteComponent implements GameComponent {

    public static final String TYPE = "SpriteComponent";

    private GameObjectInstance objectInstance;

    SpriteComponent(GameObjectInstance aObjectInstance) {
        objectInstance = aObjectInstance;
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<>();
        theResult.put(TYPE_ATTRIBUTE, TYPE);
        return theResult;
    }

    public static SpriteComponent deserialize(GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        return new SpriteComponent(aObjectInstance);
    }
}