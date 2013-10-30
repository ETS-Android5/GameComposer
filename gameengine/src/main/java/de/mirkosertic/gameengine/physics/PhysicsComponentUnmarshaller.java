package de.mirkosertic.gameengine.physics;

import java.util.Map;

import de.mirkosertic.gameengine.core.GameComponentUnmarshaller;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameRuntime;

public class PhysicsComponentUnmarshaller implements GameComponentUnmarshaller<PhysicsComponent> {

    @Override
    public String getTypeKey() {
        return PhysicsComponent.TYPE;
    }

    @Override
    public PhysicsComponent deserialize(GameRuntime aRuntime, GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        PhysicsComponent theResult = PhysicsComponent.deserialize(aObjectInstance, aSerializedData);
        return theResult;
    }
}
