/*
 * Copyright 2016 Mirko Sertic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mirkosertic.gameengine.gwt;

import com.google.gwt.media.client.Audio;
import de.mirkosertic.gameengine.core.GameResource;
import de.mirkosertic.gameengine.core.GameResourceCache;
import de.mirkosertic.gameengine.core.GameResourceLoader;
import de.mirkosertic.gameengine.sound.GameSoundSystem;
import de.mirkosertic.gameengine.type.ResourceName;

public class GWTGameSoundSystem implements GameSoundSystem<Audio> {

    private final GameResourceCache resourceCache;

    GWTGameSoundSystem(GameResourceCache aResourceCache) {
        resourceCache = aResourceCache;
    }

    @Override
    public void play(ResourceName aResourceName, final Listener<Audio> aListener) {
        try {
            resourceCache.getResourceFor(aResourceName, new GameResourceLoader.Listener() {
                @Override
                public void handle(GameResource aResource) {
                    Audio theAudio = ((GWTAudioResource) aResource).getAudio();
                    theAudio.play();
                    aListener.handle(theAudio);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop(Audio aSoundObject) {
    }
}
