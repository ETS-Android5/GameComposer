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
package de.mirkosertic.gameengine.teavm.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface LoaderResource extends JSObject {

    @JSProperty
    JSObject getData();

    @JSProperty
    void setData(JSObject aData);

    @JSProperty
    String getUrl();

    @JSProperty
    void setUrl();

    @JSProperty(value = "onComplete")
    LoaderPromise onComplete();

    void complete();
}
