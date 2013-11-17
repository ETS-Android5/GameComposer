package de.mirkosertic.gameengine.sprites;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.GameComponentTemplate;
import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameRuntime;
import de.mirkosertic.gameengine.types.ResourceName;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.Property;

public class SpriteComponentTemplate extends GameComponentTemplate<SpriteComponent> {

    private final Property<ResourceName> resourceName;
    private final GameObject owner;

    public SpriteComponentTemplate(GameEventManager aEventManager, GameObject aOwner) {
        resourceName = registerProperty(new Property<ResourceName>(this, "resourceName", aEventManager));
        owner = aOwner;
    }

    @Override
    public GameObject getOwner() {
        return owner;
    }

    public SpriteComponent create(GameObjectInstance aInstance, GameRuntime aGameRuntime) {
        return new SpriteComponent(aInstance);
    }

    @Override
    public String getTypeKey() {
        return SpriteComponent.TYPE;
    }

    public Property<ResourceName> resourceNameProperty() {
        return resourceName;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(SpriteComponent.TYPE_ATTRIBUTE, SpriteComponent.TYPE);
        if (resourceName.get() != null) {
            theResult.put("resourcename", resourceName.get().serialize());
        }
        return theResult;
    }

    public static SpriteComponentTemplate deserialize(GameEventManager aEventManager, GameObject aOwner, Map<String, Object> aSerializedData) {
        SpriteComponentTemplate theResult = new SpriteComponentTemplate(aEventManager, aOwner);
        Map<String, Object> theResourceName = (Map<String, Object>) aSerializedData.get("resourcename");
        if (theResourceName != null) {
            theResult.resourceName.setQuietly(ResourceName.deserialize(theResourceName));
        }
        return theResult;
    }
}
