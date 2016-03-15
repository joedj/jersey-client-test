package net.joedj.test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class LogRecorder extends EventEvaluatorBase<ILoggingEvent> {

    private static final Set<ILoggingEvent> EVENTS = new ConcurrentSkipListSet<ILoggingEvent>();

    public static void clear() {
        EVENTS.clear();
    }

    public static Set<ILoggingEvent> events() {
        return new HashSet<ILoggingEvent>(EVENTS);
    }

    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        EVENTS.add(event);
        return true;
    }

}
