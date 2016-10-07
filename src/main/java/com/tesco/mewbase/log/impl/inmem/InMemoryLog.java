package com.tesco.mewbase.log.impl.inmem;

import com.tesco.mewbase.bson.BsonObject;
import com.tesco.mewbase.common.ReadStream;
import com.tesco.mewbase.common.WriteStream;
import com.tesco.mewbase.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tim on 27/09/16.
 */
public class InMemoryLog implements Log {

    private final static Logger log = LoggerFactory.getLogger(InMemoryLog.class);

    private final Queue<BsonObject> queue = new ConcurrentLinkedQueue<>();

    public CompletableFuture<Void> append(BsonObject obj) {
        queue.add(obj);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        cf.complete(null);
        log.trace("Appended obj {}", obj);

        return cf;
    }

    @Override
    public ReadStream openReadStream(long pos) {
        BsonObject top = queue.element();
        Iterator<BsonObject> iter = queue.iterator();
        if (top.getLong("pos") < pos) {
            while (iter.hasNext()) {
                BsonObject obj = iter.next();
                if (obj.getLong("pos") == pos - 1) {
                    break;
                }
            }
        }
        return new InMemoryReadStream(iter);
    }

    @Override
    public WriteStream openWriteStream() {
        return new InMemoryWriteStream(queue);
    }
}
