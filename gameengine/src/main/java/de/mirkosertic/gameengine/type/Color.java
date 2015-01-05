package de.mirkosertic.gameengine.type;

import de.mirkosertic.gameengine.annotations.ReflectiveField;

import java.util.HashMap;
import java.util.Map;

public class Color implements Reflectable<ColorClassInformation> {

    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0);

    @ReflectiveField
    public final int r;

    @ReflectiveField
    public final int g;

    @ReflectiveField
    public final int b;

    public Color(int aR, int aG, int aB) {
        r = aR;
        g = aG;
        b = aB;
    }

    @Override
    public ColorClassInformation getClassInformation() {
        return ColorClassInformation.INSTANCE;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<>();
        theResult.put("r", Integer.toString(r));
        theResult.put("g", Integer.toString(g));
        theResult.put("b", Integer.toString(b));
        return theResult;
    }

    public static Color deserialize(Map<String, Object> aSerializedData) {
        int theR = Integer.parseInt((String) aSerializedData.get("r"));
        int theG = Integer.parseInt((String) aSerializedData.get("g"));
        int theB = Integer.parseInt((String) aSerializedData.get("b"));
        return new Color(theR, theG, theB);
    }
}
