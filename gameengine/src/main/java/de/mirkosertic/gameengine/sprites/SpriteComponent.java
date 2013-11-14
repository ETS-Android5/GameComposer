package de.mirkosertic.gameengine.sprites;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.GameComponent;
import de.mirkosertic.gameengine.core.GameObjectInstance;

public class SpriteComponent implements GameComponent {

    public static final String TYPE = "SpriteComponent";

    private final GameObjectInstance objectInstance;

    SpriteComponent(GameObjectInstance aObjectInstance) {
        objectInstance = aObjectInstance;
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(TYPE_ATTRIBUTE, TYPE);
        return theResult;
    }

    @Override
    public SpriteComponentTemplate getTemplate() {
        return objectInstance.getOwnerGameObject().getComponentTemplate(SpriteComponentTemplate.class);
    }

    public static SpriteComponent deserialize(GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        return new SpriteComponent(aObjectInstance);
    }
}