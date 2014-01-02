package de.mirkosertic.gameengine.text;

import de.mirkosertic.gameengine.core.GameComponent;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.Property;
import de.mirkosertic.gameengine.type.Color;
import de.mirkosertic.gameengine.type.Font;
import de.mirkosertic.gameengine.type.TextExpression;

import java.util.HashMap;
import java.util.Map;

public class TextComponent extends GameComponent implements Text {

    static final String TYPE = "TextComponent";

    private final GameObjectInstance objectInstance;

    private final Property<Font> font;
    private final Property<Color> color;
    private final Property<TextExpression> textExpression;

    TextComponent(GameObjectInstance aObjectInstance) {
        this(aObjectInstance, aObjectInstance.getOwnerGameObject().getComponentTemplate(TextComponentTemplate.class));
    }

    TextComponent(GameObjectInstance aObjectInstance, TextComponentTemplate aTemplate) {

        GameEventManager theEventManager = aObjectInstance.getOwnerGameObject().getGameScene().getRuntime().getEventManager();

        objectInstance = aObjectInstance;
        font = registerProperty(new Property<Font>(this, FONT_PROPERTY, aTemplate.fontProperty().get(), theEventManager));
        color = registerProperty(new Property<Color>(this, COLOR_PROPERTY, aTemplate.colorProperty().get(), theEventManager));
        textExpression = registerProperty(new Property<TextExpression>(this, TEXT_EXPRESSION_PROPERTY, aTemplate.textExpressionProperty().get(), theEventManager));
    }

    public Property<Font> fontProperty() {
        return font;
    }

    public Property<Color> colorProperty() {
        return color;
    }

    public Property<TextExpression> textExpressionProperty() {
        return textExpression;
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(TYPE_ATTRIBUTE, TYPE);
        theResult.put(FONT_PROPERTY, font.get().serialize());
        theResult.put(TEXT_EXPRESSION_PROPERTY, textExpression.get().serialize());
        theResult.put(COLOR_PROPERTY, color.get().serialize());
        return theResult;
    }

    @Override
    public TextComponentTemplate getTemplate() {
        return objectInstance.getOwnerGameObject().getComponentTemplate(TextComponentTemplate.class);
    }

    public static TextComponent deserialize(GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        TextComponent theTemplate =  new TextComponent(aObjectInstance);
        theTemplate.font.setQuietly(Font.deserialize((Map<String, Object>) aSerializedData.get(FONT_PROPERTY)));
        theTemplate.textExpression.setQuietly(TextExpression.deserialize((Map<String, Object>) aSerializedData.get(TEXT_EXPRESSION_PROPERTY)));
        theTemplate.color.setQuietly(Color.deserialize((Map<String, Object>) aSerializedData.get(COLOR_PROPERTY)));
        return theTemplate;
    }
}