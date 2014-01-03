package de.mirkosertic.gameengine.playerscore;

import de.mirkosertic.gameengine.core.GameComponent;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.Property;
import de.mirkosertic.gameengine.type.ScoreValue;

import java.util.HashMap;
import java.util.Map;

public class PlayerScoreComponent extends GameComponent implements PlayerScore {

    static final String TYPE = "PlayerScoreComponent";

    private final GameObjectInstance objectInstance;
    private final Property<ScoreValue> scoreValue;

    private PlayerScoreComponent(GameObjectInstance aObjectInstance) {
        this(aObjectInstance, aObjectInstance.getOwnerGameObject().getComponentTemplate(PlayerScoreComponentTemplate.class));
    }

    PlayerScoreComponent(GameObjectInstance aObjectInstance, PlayerScoreComponentTemplate aTemplate) {
        objectInstance = aObjectInstance;

        GameEventManager theEventManager = aObjectInstance.getOwnerGameObject().getGameScene().getRuntime().getEventManager();

        scoreValue = registerProperty(new Property<ScoreValue>(this, SCORE_VALUE_PROPERTY, aTemplate.scoreValueProperty().get(), theEventManager));
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    @Override
    public PlayerScoreComponentTemplate getTemplate() {
        return objectInstance.getOwnerGameObject().getComponentTemplate(PlayerScoreComponentTemplate.class);
    }

    @Override
    public Property<ScoreValue> scoreValueProperty() {
        return scoreValue;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(SCORE_VALUE_PROPERTY, scoreValue.get().serialize());
        return theResult;
    }

    public static PlayerScoreComponent deserialize(GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        PlayerScoreComponent theComponent = new PlayerScoreComponent(aObjectInstance);
        Map<String, Object> theScore = (Map<String, Object>) aSerializedData.get(SCORE_VALUE_PROPERTY);
        theComponent.scoreValue.setQuietly(ScoreValue.deserialize(theScore));
        return theComponent;
    }
}
