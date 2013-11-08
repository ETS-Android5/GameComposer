package de.mirkosertic.gameengine.core;

import java.util.*;

public class GameObjectInstance {

    private Map<Class<GameComponent>, GameComponent> components;
    private Position position;
    private String name;
    private GameObject ownerGameObject;
    private Angle rotationAngle;

    GameObjectInstance(GameObject aOwnerGameObject) {
        ownerGameObject = aOwnerGameObject;
        components = new HashMap<Class<GameComponent>, GameComponent>();
        position = new Position();
        rotationAngle = new Angle(0);
    }

    public boolean contains(Position aPosition) {
        Size theSize = ownerGameObject.getSize();
        return (aPosition.x >= position.x && aPosition.y >= position.y &&
                aPosition.x <= position.x + theSize.width &&
                aPosition.y <= position.y + theSize.height);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Angle getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(Angle rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public GameObject getOwnerGameObject() {
        return ownerGameObject;
    }

    void addComponent(GameComponent aComponent) {
        components.put((Class<GameComponent>) aComponent.getClass(), aComponent);
    }

    public <T extends GameComponent> T getComponent(Class<T> aComponentClass) {
        return (T) components.get(aComponentClass);
    }

    public Set<GameComponent> getComponents() {
        HashSet<GameComponent> theResult = new HashSet<GameComponent>(components.values());
        return Collections.unmodifiableSet(theResult);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put("gameobjectuuid", ownerGameObject.getUuid());

        theResult.put("position", position.serializeToMap());
        theResult.put("name", name);
        theResult.put("rotationangle", rotationAngle.serialize());

        List<Map<String, Object>> theComponents = new ArrayList<Map<String, Object>>();
        for (GameComponent theComponent : components.values()) {
            theComponents.add(theComponent.serialize());
        }
        theResult.put("components", theComponents);

        return theResult;
    }

    public static GameObjectInstance deserialize(GameRuntime aGameRuntime, GameScene aScene, Map<String, Object> theInstance) {

        String theUUID = (String) theInstance.get("gameobjectuuid");
        GameObject theGameObject = aScene.findGameObjectByID(theUUID);
        if (theGameObject == null) {
            throw new RuntimeException("Cannot find gameobject with uuid" + theUUID);
        }

        GameObjectInstance theResult = new GameObjectInstance(theGameObject);
        theResult.position = Position.deserialize((Map<String, Object>) theInstance.get("position"));
        theResult.name = (String) theInstance.get("name");

        Map<String, Object> theRotationAngle = (Map<String, Object>) theInstance.get("rotationangle");
        if (theRotationAngle != null) {
            theResult.rotationAngle = Angle.deserialize(theRotationAngle);
        }

        List<Map<String, Object>> theComponents = (List<Map<String, Object>>) theInstance.get("components");
        for (Map<String, Object> theStructure : theComponents) {
            String theType = (String) theStructure.get(GameComponent.TYPE_ATTRIBUTE);

            GameComponentUnmarshaller theUnmarshaller = aGameRuntime.getComponentUnmarshallerFor(theType);
            theResult.addComponent(theUnmarshaller.deserialize(aGameRuntime, theResult, theStructure));
        }

        return theResult;
    }
}
