package de.mirkosertic.gameengine.core;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void testNameProperty() throws Exception {
        Game theGame = new Game();
        assertEquals(Game.NAME_PROPERTY, theGame.nameProperty().getName());
        assertEquals(String.class, theGame.nameProperty().getType());
        assertNull(theGame.nameProperty().get());
        assertSame(theGame, theGame.nameProperty().getOwner());
    }

    @Test
    public void testDefaultSceneProperty() throws Exception {
        Game theGame = new Game();
        assertEquals(Game.DEFAULT_SCENE_PROPERTY, theGame.defaultSceneProperty().getName());
        assertEquals(String.class, theGame.defaultSceneProperty().getType());
        assertNull(theGame.defaultSceneProperty().get());
        assertSame(theGame, theGame.defaultSceneProperty().getOwner());
    }

    @Test
    public void testEnableWebGLProperty() throws Exception {
        Game theGame = new Game();
        assertEquals(Game.ENABLE_WEB_GL_PROPERTY, theGame.enableWebGLProperty().getName());
        assertEquals(Boolean.class, theGame.enableWebGLProperty().getType());
        assertTrue(theGame.enableWebGLProperty().get());
        assertSame(theGame, theGame.enableWebGLProperty().getOwner());
    }

    @Test
    public void testRemoveScene() throws Exception {
        Game theGame = new Game();
        theGame.defaultSceneProperty().set("test");
        theGame.removeScene("scene1");
        assertEquals("test", theGame.defaultSceneProperty().get());
        theGame.removeScene("test");
        assertNull(theGame.defaultSceneProperty().get());
    }

    @Test
    public void testSerialize() throws Exception {
        Game theGame = new Game();
        theGame.nameProperty().set("Testgame");
        theGame.defaultSceneProperty().set("scene1");
        theGame.customPropertiesProperty().get().set("key", "value");
        Map<String, Object> theData = theGame.serialize();
        assertEquals(4, theData.size());
        assertEquals("Testgame", theData.get(Game.NAME_PROPERTY));
        assertEquals("scene1", theData.get("defaultscene"));
        assertEquals("true", theData.get("enablewebgl"));

        Map<String, String> theProps = (Map<String, String>) theData.get("customProperties");
        assertEquals(1, theProps.size(), 0);
        assertEquals("value", theProps.get("key"));
    }

    @Test
    public void testDeserialize1() throws Exception {
        Map<String, Object> theData = new HashMap<>();
        theData.put(Game.NAME_PROPERTY, "Testgame");
        theData.put("defaultscene", "scene1");
        Game theGame = Game.deserialize(theData);
        assertEquals("Testgame", theGame.nameProperty().get());
        assertEquals("scene1", theGame.defaultSceneProperty().get());
        assertTrue(theGame.enableWebGLProperty().get());
    }

    @Test
    public void testDeserialize2() throws Exception {
        Map<String, Object> theData = new HashMap<>();
        theData.put(Game.NAME_PROPERTY, "Testgame");
        theData.put("defaultscene", "scene1");
        theData.put("enablewebgl", "false");

        Map<String, String> theCustomProperties = new HashMap<>();
        theCustomProperties.put("key", "value");
        theData.put("customProperties", theCustomProperties);

        Game theGame = Game.deserialize(theData);
        assertEquals("Testgame", theGame.nameProperty().get());
        assertEquals("scene1", theGame.defaultSceneProperty().get());
        assertFalse(theGame.enableWebGLProperty().get());
        assertEquals("value", theGame.customPropertiesProperty().get().get("key"));
    }
}