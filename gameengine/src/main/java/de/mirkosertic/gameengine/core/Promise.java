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
package de.mirkosertic.gameengine.core;

import java.util.ArrayList;
import java.util.List;

public class Promise<T,V> implements PromiseResolver<T>, PromiseRejector<V> {

    public enum State {
        pending,
        resolved,
        rejected;
    }

    public interface Executor<T,V> {
        void process(PromiseResolver<T> aResolver, PromiseRejector<V> aRejector);
    }

    public interface Handler<X,Y> {
        Y process(X aResult);
    }

    public interface NoReturnHandler<X> {
        void process(X aResult);
    }

    public interface ErrorHandler<X> {
        void process(X aResult, Exception aOptionalException);
    }

    private State state;
    private T resolvedValue;
    private V rejectedReason;
    private Exception optionalRejectedException;

    private final List<Promise<T,V>> followups;

    public Promise() {
        this(new Executor<T, V>() {
            @Override
            public void process(PromiseResolver<T> aResolver, PromiseRejector<V> aRejector) {
            }
        });
    }

    public Promise(Executor aExecutor) {
        state = State.pending;
        followups = new ArrayList<>();
        aExecutor.process(this, this);
    }

    public Promise.State getState() {
        return state;
    }

    public T getResolvedValue() {
        return resolvedValue;
    }

    public V getRejectedReason() {
        return rejectedReason;
    }

    public Exception getOptionalRejectedException() {
        return optionalRejectedException;
    }

    @Override
    public void resolve(T aValue) {
        if (state != State.pending) {
            throw new IllegalStateException("Only pending promises can be resolved");
        }
        state = State.resolved;
        resolvedValue = aValue;
        for (Promise<T,V> theFollowUp : followups) {
            theFollowUp.resolve(aValue);
        }
    }

    @Override
    public void reject(V aReason, Exception aOptionalExceptiopn) {
        if (state != State.pending) {
            throw new IllegalStateException("Only pending promises can be rejected");
        }
        state = State.rejected;
        rejectedReason = aReason;
        optionalRejectedException = aOptionalExceptiopn;
        for (Promise<T,V> theFollowUp: followups) {
            theFollowUp.reject(aReason, aOptionalExceptiopn);
        }
    }

    public <X> Promise<X, V> thenContinue(final Handler<T, X> aFilter) {
        final Promise<X, V> theNewPromise = new Promise<>();
        switch (state) {
            case pending:
                addFollowUp(new Promise<T, V>() {
                    @Override
                    public void resolve(T aValue) {
                        theNewPromise.resolve(aFilter.process(aValue));
                        super.resolve(aValue);
                    }

                    @Override
                    public void reject(V aReason, Exception aOptionalRejectedException) {
                        super.reject(aReason, aOptionalRejectedException);
                        Promise.this.reject(aReason, aOptionalRejectedException);
                    }
                });
                break;
            case resolved:
                theNewPromise.resolve(aFilter.process(resolvedValue));
                break;
            case rejected:
                theNewPromise.reject(rejectedReason, optionalRejectedException);
                break;
        }
        return theNewPromise;
    }

    public Promise<T, V> thenContinue(final NoReturnHandler<T> aFilter) {
        switch (state) {
            case pending:
                return addFollowUp(new Promise<T, V>() {
                    @Override
                    public void resolve(T aValue) {
                        aFilter.process(aValue);
                        super.resolve(aValue);
                    }

                    @Override
                    public void reject(V aReason, Exception aOptionalRejectedException) {
                        super.reject(aReason, aOptionalRejectedException);
                        Promise.this.reject(aReason, aOptionalRejectedException);
                    }
                });
            case resolved:
                Promise<T,V> theResult = new Promise<T, V>() {
                    @Override
                    public void resolve(T aValue) {
                        aFilter.process(aValue);
                        super.resolve(aValue);
                    }

                    @Override
                    public void reject(V aReason, Exception aOptionalRejectedException) {
                        super.reject(aReason, aOptionalRejectedException);
                        Promise.this.reject(aReason, aOptionalRejectedException);
                    }
                };
                theResult.resolve(resolvedValue);
                return theResult;
            case rejected:
                Promise<T,V> thePromise = new Promise<>();
                thePromise.reject(rejectedReason, optionalRejectedException);
                return thePromise;
        }
        throw new IllegalStateException();
    }

    public void catchError(final ErrorHandler<V> aFilter) {
        switch (state) {
            case pending:
                addFollowUp(new Promise<T, V>() {
                    @Override
                    public void reject(V aReason, Exception aOptionalRejectedException) {
                        aFilter.process(aReason, aOptionalRejectedException);
                        super.reject(aReason, aOptionalRejectedException);
                    }
                });
                break;
            case rejected:
                aFilter.process(rejectedReason, optionalRejectedException);
                break;
            case resolved:
                // Nothing to do
                break;
        }
    }

    private Promise<T,V> addFollowUp(Promise<T,V> aFollowUp) {
        followups.add(aFollowUp);
        return aFollowUp;
    }

    public static Promise<Promise[], Void> all(final Promise... aPromise) {
        final Promise<Promise[], Void> theUnionPromise = new Promise();

        final Promise[] theData = new Promise[aPromise.length];
        for (int i=0;i<aPromise.length;i++) {
            final int theFinalI = i;
            final Promise thePromise = aPromise[i];
            thePromise.thenContinue(new NoReturnHandler() {
                @Override
                public void process(Object aResult) {
                    theData[theFinalI] = thePromise;
                    boolean theComplete = true;
                    for (int k=0;k<aPromise.length;k++) {
                        if (theData[k] == null) {
                            theComplete = false;
                        }
                    }
                    if (theComplete) {
                        theUnionPromise.resolve(theData);
                    }
                }
            }).catchError(new ErrorHandler() {
                @Override
                public void process(Object aResult, Exception aOptionalException) {
                    theUnionPromise.reject(null, aOptionalException);
                }
            });
        }

        return theUnionPromise;
    }
}
