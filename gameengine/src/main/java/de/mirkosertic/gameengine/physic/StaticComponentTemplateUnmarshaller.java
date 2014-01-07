package de.mirkosertic.gameengine.physic;

import java.util.Map;

import de.mirkosertic.gameengine.core.GameComponentTemplateUnmarshaller;
import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.event.GameEventManager;

public class StaticComponentTemplateUnmarshaller implements GameComponentTemplateUnmarshaller<StaticComponentTemplate> {

    @Override
    public String getTypeKey() {
        return StaticComponent.TYPE;
    }

    @Override
    public StaticComponentTemplate deserialize(GameEventManager aEventmanager, GameObject aOwner, Map<String, Object> aSerializedData) {
        return StaticComponentTemplate.deserialize(aEventmanager, aOwner);
    }
}
