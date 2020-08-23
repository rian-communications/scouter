/*
 *  Copyright 2015 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package scouter.xtra.java8;

import com.mongodb.internal.async.SingleResultCallback;
import com.mongodb.internal.connection.SplittablePayload;
import org.bson.BsonDocument;
import scouter.agent.Configure;
import scouter.agent.Logger;
import scouter.agent.proxy.IMongoDbTracer;
import scouter.agent.trace.StepTransferMap;
import scouter.agent.trace.TraceContext;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2020/08/16
 */
public class MongoDbTracer405 implements IMongoDbTracer {

    static Configure conf = Configure.getInstance();

    @Override
    public StepTransferMap.ID generateAndTransferMongoQueryStep(TraceContext ctx, Object _this, Object connection) {
        return MongoDbTracer.generateAndTransferMongoQueryStep(ctx, _this, connection);
    }

    @Override
    public Object wrapCallback(StepTransferMap.ID id, Object namespace, Object command, Object readPreference, Object payload, Object callback) {
        if (id == null) {
            return callback;
        }
        try {
            if (callback instanceof SingleResultCallback) {
                List<BsonDocument> payload0 = null;
                if (payload instanceof SplittablePayload) {
                    payload0 = ((SplittablePayload) payload).getPayload();
                }
                return new ScMongoSingleResultCallback405(id, (SingleResultCallback) callback, namespace, command, readPreference, payload0);
            } else {
                return callback;
            }
        } catch (Throwable e) {
            Logger.println("MDp02", e.getMessage(), e);
            return callback;
        }
    }

    public static class ScMongoSingleResultCallback405<T> extends MongoDbTracer.ScMongoSingleResultCallback<T>
            implements SingleResultCallback<T> {

        public SingleResultCallback<T> inner;

        public ScMongoSingleResultCallback405(StepTransferMap.ID id, SingleResultCallback<T> callback, Object namespace,
                                              Object command, Object readPreference, List<BsonDocument> payload) {
            super(id, namespace, command, readPreference, payload);
            this.inner = callback;
        }

        @Override
        public void onResult(T result, Throwable t) {
            endMongoQueryStep(t);
            inner.onResult(result, t);
        }
    }
}
