package de.mirkosertic.gameengine.physics.jbox2d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.event.GameEventListener;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.PropertyChangeEvent;
import de.mirkosertic.gameengine.physics.GameObjectCollisionEvent;
import de.mirkosertic.gameengine.physics.GamePhysicsManager;
import de.mirkosertic.gameengine.physics.PhysicsComponent;
import de.mirkosertic.gameengine.physics.PhysicsComponentTemplate;
import de.mirkosertic.gameengine.physics.PhysicsDebugCanvas;
import de.mirkosertic.gameengine.physics.PlatformComponent;
import de.mirkosertic.gameengine.physics.StaticComponent;
import de.mirkosertic.gameengine.types.Angle;
import de.mirkosertic.gameengine.types.Position;
import de.mirkosertic.gameengine.types.Size;

public class JBox2DGamePhysicsManager implements GamePhysicsManager {

    private class PositionChangeListener implements GameEventListener<PropertyChangeEvent> {
        @Override
        public void handleGameEvent(PropertyChangeEvent aEvent) {
            GameObjectInstance theInstance = (GameObjectInstance) aEvent.getOwner();
            Body theOldBody = gameObjectInstanceRemovedFromScene(theInstance);
            Body theNewBody = gameObjectInstanceAddedToScene(theInstance);
            if (theOldBody != null && theNewBody != null) {
                theNewBody.setActive(theOldBody.isActive());
            }
        }
    }

    private class SizeChangeListener implements GameEventListener<PropertyChangeEvent> {
        @Override
        public void handleGameEvent(PropertyChangeEvent aEvent) {
            GameObject theChangedObject = (GameObject) aEvent.getOwner();

            Set<GameObjectInstance> theChangedInstances = new HashSet<GameObjectInstance>();
            for (GameObjectInstance theObjectInstance : dynamicObjects.keySet()) {
                if (theObjectInstance.getOwnerGameObject() == theChangedObject) {
                    theChangedInstances.add(theObjectInstance);
                }
            }
            for (GameObjectInstance theObjectInstance : staticObjects.keySet()) {
                if (theObjectInstance.getOwnerGameObject() == theChangedObject) {
                    theChangedInstances.add(theObjectInstance);
                }
            }

            for (GameObjectInstance theInstance : theChangedInstances) {
                Body theOldBody = gameObjectInstanceRemovedFromScene(theInstance);
                Body theNewBody = gameObjectInstanceAddedToScene(theInstance);
                if (theOldBody != null && theNewBody != null) {
                    theNewBody.setActive(theOldBody.isActive());
                }
            }
        }
    }

    private static final float SIZE_FACTOR = 0.01f;

    private World physicsWorld;
    private Map<GameObjectInstance, Body> dynamicObjects;
    private Map<GameObjectInstance, Body> staticObjects;
    private long physicsAmountOfTime;
    private GameEventManager eventManager;
    private PositionChangeListener positionChangeListener;
    private SizeChangeListener sizeChangeListener;
    private Set<GameObject> alreadyRegisteredSizeListener;

    JBox2DGamePhysicsManager(GameEventManager aEventManager) {
        eventManager = aEventManager;
        alreadyRegisteredSizeListener = new HashSet<GameObject>();
        positionChangeListener = new PositionChangeListener();
        sizeChangeListener = new SizeChangeListener();

        // This is the gravity vector, it goes down in our coordinate system, of course
        Vec2 gravity = new Vec2(0.0f, -10.0f);
        physicsWorld = new World(gravity);
        physicsWorld.setContactListener(new ContactListener() {
            public void beginContact(Contact aContact) {
                Body theObjectA = aContact.getFixtureA().getBody();
                Body theObjectB = aContact.getFixtureB().getBody();
                eventManager.fire(new GameObjectCollisionEvent((GameObjectInstance) theObjectA.getUserData(),
                        (GameObjectInstance) theObjectB.getUserData()));
            }

            public void endContact(Contact aContact) {
            }

            public void preSolve(Contact aContact, Manifold aOldManifold) {
            }

            public void postSolve(Contact aContact, ContactImpulse aImpulse) {
            }
        });
        // Dynamic objects must be awake by default, or they will wait for a kind of impulse
        physicsWorld.setAllowSleep(false);
        dynamicObjects = new HashMap<GameObjectInstance, Body>();
        staticObjects = new HashMap<GameObjectInstance, Body>();
        physicsAmountOfTime = 0;
    }

    Body gameObjectInstanceRemovedFromScene(GameObjectInstance aInstance) {
        synchronized (physicsWorld) {
            Body theSimulatedBody = dynamicObjects.remove(aInstance);
            aInstance.positionProperty().removeChangeListener(positionChangeListener);
            if (theSimulatedBody == null) {
                theSimulatedBody = staticObjects.remove(aInstance);
            }
            if (theSimulatedBody != null) {
                physicsWorld.destroyBody(theSimulatedBody);
                return theSimulatedBody;
            }
        }
        return null;
    }

    Body gameObjectInstanceAddedToScene(GameObjectInstance aInstance) {
        synchronized (physicsWorld) {
            aInstance.positionProperty().addChangeListener(positionChangeListener);
            aInstance.rotationAngleProperty().addChangeListener(positionChangeListener);
            if (alreadyRegisteredSizeListener.add(aInstance.getOwnerGameObject())) {
                aInstance.getOwnerGameObject().sizeProperty().addChangeListener(sizeChangeListener);
            }

            Position theInstancePosition = aInstance.positionProperty().get();
            Size theInstanceSize = aInstance.getOwnerGameObject().sizeProperty().get();

            // Check if is a static component
            StaticComponent theStaticComponent = aInstance.getComponent(StaticComponent.class);
            if (theStaticComponent != null) {
                // The component is static hence not moveable, we add it as a ground to the physics simulation
                PolygonShape theStaticShape = new PolygonShape();
                theStaticShape.setAsBox(SIZE_FACTOR * theInstanceSize.width / 2, SIZE_FACTOR * theInstanceSize.height
                        / 2);

                FixtureDef theStaticFixture = new FixtureDef();
                theStaticFixture.shape = theStaticShape;
                theStaticFixture.density = 1;
                theStaticFixture.friction = 1.8f;
                theStaticFixture.restitution = 0;

                BodyDef theStaticBodyDef = new BodyDef();
                theStaticBodyDef.type = BodyType.STATIC;
                theStaticBodyDef.userData = aInstance;
                theStaticBodyDef.setAngle(aInstance.rotationAngleProperty().get().invert().toRadians());
                // The position is the CENTER of MASS, not the X/Y Coordinate
                theStaticBodyDef.position = new Vec2(SIZE_FACTOR * (theInstancePosition.x + theInstanceSize.width / 2),
                        -SIZE_FACTOR * (theInstancePosition.y + theInstanceSize.height / 2));
                Body theBody = physicsWorld.createBody(theStaticBodyDef);
                theBody.createFixture(theStaticFixture);

                staticObjects.put(aInstance, theBody);

                return theBody;
            }

            // Now the player, hence the platform component
            PlatformComponent thePlatformComponent = aInstance.getComponent(PlatformComponent.class);
            if (thePlatformComponent != null) {
                PolygonShape thePlatformShape = new PolygonShape();
                thePlatformShape.setAsBox(SIZE_FACTOR * theInstanceSize.width / 2, SIZE_FACTOR * theInstanceSize.height
                        / 2);

                FixtureDef thePlatformFixture = new FixtureDef();
                thePlatformFixture.density = 1;
                thePlatformFixture.friction = 0.1f;
                thePlatformFixture.restitution = 0;
                thePlatformFixture.shape = thePlatformShape;

                BodyDef thePlatformBodyDef = new BodyDef();
                thePlatformBodyDef.type = BodyType.DYNAMIC;
                thePlatformBodyDef.setAngle(aInstance.rotationAngleProperty().get().invert().toRadians());
                // The position is the CENTER of MASS, not the X/Y Coordinate
                thePlatformBodyDef.position = new Vec2(SIZE_FACTOR
                        * (theInstancePosition.x + theInstanceSize.width / 2), SIZE_FACTOR
                        * (theInstancePosition.y + theInstanceSize.height / 2));
                thePlatformBodyDef.userData = aInstance;

                Body thePlatformBody = physicsWorld.createBody(thePlatformBodyDef);
                dynamicObjects.put(aInstance, thePlatformBody);
                thePlatformBody.createFixture(thePlatformFixture);

                return thePlatformBody;
            }

            // Additional physics
            PhysicsComponent thePhysicscomponent = aInstance.getComponent(PhysicsComponent.class);
            if (thePhysicscomponent != null) {

                PhysicsComponentTemplate theTemplate = aInstance.getOwnerGameObject().getComponentTemplate(
                        PhysicsComponentTemplate.class);

                PolygonShape thePhysicsShape = new PolygonShape();
                thePhysicsShape.setAsBox(SIZE_FACTOR * theInstanceSize.width / 2, SIZE_FACTOR * theInstanceSize.height
                        / 2);

                FixtureDef thePhysicsFixture = new FixtureDef();
                thePhysicsFixture.density = 1;
                thePhysicsFixture.friction = 0.1f;
                thePhysicsFixture.restitution = 0;
                thePhysicsFixture.shape = thePhysicsShape;

                BodyDef thePhysicsBodyDef = new BodyDef();
                thePhysicsBodyDef.type = BodyType.DYNAMIC;
                // The position is the CENTER of MASS, not the X/Y Coordinate
                thePhysicsBodyDef.position = new Vec2(
                        SIZE_FACTOR * (theInstancePosition.x + theInstanceSize.width / 2), SIZE_FACTOR
                                * (theInstancePosition.y + theInstanceSize.height / 2));
                thePhysicsBodyDef.userData = aInstance;
                thePhysicsBodyDef.setAngle(aInstance.rotationAngleProperty().get().invert().toRadians());
                thePhysicsBodyDef.setFixedRotation(theTemplate.fixedRotationProperty().get());

                Body thePhysicsBody = physicsWorld.createBody(thePhysicsBodyDef);
                dynamicObjects.put(aInstance, thePhysicsBody);
                thePhysicsBody.createFixture(thePhysicsFixture);

                return thePhysicsBody;
            }
        }
        return null;
    }

    void applyImpulse(GameObjectInstance aInstance, float aImpulseX, float aImpulseY) {
        synchronized (physicsWorld) {
            Body theBody = dynamicObjects.get(aInstance);
            if (theBody != null) {
                theBody.applyLinearImpulse(new Vec2(aImpulseX, aImpulseY), theBody.getWorldCenter());
            }
        }
    }

    void applyForce(GameObjectInstance aInstance, float aForceX, float aForceY) {
        synchronized (physicsWorld) {
            Body theBody = dynamicObjects.get(aInstance);
            if (theBody != null) {
                theBody.applyForce(new Vec2(aForceX, aForceY), theBody.getWorldCenter());
            }
        }
    }

    void disableDynamicPhysicsOn(GameObjectInstance aInstance) {
        Body theSimulatedBody = dynamicObjects.get(aInstance);
        if (theSimulatedBody != null) {
            theSimulatedBody.setActive(false);
        }
    }

    void enableDynamicPhysicsOn(GameObjectInstance aInstance) {
        Body theSimulatedBody = dynamicObjects.get(aInstance);
        if (theSimulatedBody != null) {
            theSimulatedBody.setActive(true);
        }
    }

    public void updateGameObjectConfiguration(GameObject aGameObject) {
        for (GameObjectInstance theInstance : aGameObject.getGameScene().getInstances()) {
            if (theInstance.getOwnerGameObject() == aGameObject) {
                gameObjectInstanceRemovedFromScene(theInstance);
                gameObjectInstanceAddedToScene(theInstance);
            }
        }
    }

    public void proceedGame(long aGameTime, long aElapsedTimeSinceLastLoop) {

        synchronized (physicsWorld) {
            physicsAmountOfTime += aElapsedTimeSinceLastLoop;

            // We limit the physics system to 60 frames / second, or we are getting strange results
            if (physicsAmountOfTime > 16) {
                // This define how accurately velocity will be simulated. Higher iteration value increases the accuracy
                // of velocity simulation but decreases the performance. The recommended velocity iteration value is 6.
                int theVelocityIterations = 6;

                // This is similar to velocity iteration, higher value means more accurate position simulation but
                // lesser performance. The recommended position iteration value is 3.
                int thePositionIterations = 2;

                float theTimestep = 1f / 60f * physicsAmountOfTime / 16;

                // The time measure of JBox2D is 1/60 seconds
                physicsWorld.step(theTimestep, theVelocityIterations, thePositionIterations);

                // Finally, we have to update the position of our game objects to sync them to the simulation
                for (Map.Entry<GameObjectInstance, Body> theEntry : dynamicObjects.entrySet()) {
                    GameObjectInstance theGameObject = theEntry.getKey();
                    Size theInstanceSize = theGameObject.getOwnerGameObject().sizeProperty().get();
                    Body theSimulatedBody = theEntry.getValue();

                    Vec2 thePosition = theSimulatedBody.getPosition();

                    if (theSimulatedBody.isActive()) {
                        // Now we have to use the XY coordinates again
                        theGameObject.positionProperty().setQuietly(
                                new Position((thePosition.x / SIZE_FACTOR) - theInstanceSize.width / 2,
                                        -(thePosition.y / SIZE_FACTOR) - theInstanceSize.height / 2));
                        theGameObject.rotationAngleProperty().setQuietly(
                                Angle.fromRadians(theSimulatedBody.getAngle()).invert());
                    }
                }

                // Reset the time counter
                physicsAmountOfTime = 0;
            }
        }
    }

    private Position toPosition(Vec2 aVec) {
        return new Position(aVec.x / SIZE_FACTOR, -aVec.y / SIZE_FACTOR);
    }

    private Position toPosition(Vec2 aVec, Vec2 aOffset, float aAngleInRadians) {

        float theRotatedX = (float) (Math.cos(aAngleInRadians) * aVec.x - Math.sin(aAngleInRadians) * aVec.y);
        float theRotatedY = (float) (Math.sin(aAngleInRadians) * aVec.x + Math.cos(aAngleInRadians) * aVec.y);

        return new Position((theRotatedX + aOffset.x) / SIZE_FACTOR, -(theRotatedY + aOffset.y) / SIZE_FACTOR);
    }

    @Override
    public void drawDebug(PhysicsDebugCanvas aCanvas) {
        synchronized (physicsWorld) {
            Body theBody = physicsWorld.getBodyList();
            while (theBody != null) {

                float theRotatedAngle = theBody.getAngle();

                Vec2 theBodyPosition = theBody.getPosition();
                aCanvas.drawPosition(toPosition(theBodyPosition));

                Fixture theFixture = theBody.getFixtureList();
                while (theFixture != null) {
                    Shape theShape = theFixture.getShape();
                    if (theShape.getType() == ShapeType.POLYGON) {
                        PolygonShape thePolyShape = (PolygonShape) theShape;
                        for (int i = 1; i < thePolyShape.getVertexCount(); i++) {
                            aCanvas.drawLine(
                                    toPosition(thePolyShape.getVertex(i - 1), theBodyPosition, theRotatedAngle),
                                    toPosition(thePolyShape.getVertex(i), theBodyPosition, theRotatedAngle),
                                    theBody.isAwake());
                        }
                        aCanvas.drawLine(
                                toPosition(thePolyShape.getVertex(thePolyShape.getVertexCount() - 1), theBodyPosition,
                                        theRotatedAngle),
                                toPosition(thePolyShape.getVertex(0), theBodyPosition, theRotatedAngle), theBody
                                        .isAwake());
                    }
                    theFixture = theFixture.getNext();
                }

                theBody = theBody.getNext();
            }
        }
    }
}
