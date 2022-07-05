package me.wolfsurge.cerauno.listener;

import scala.collection.immutable.Stream;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * An object which holds the data of a subscribed method
 * @author Wolfsurge
 * @since 05/03/22
 */
public class SubscribedMethod {

    // Avoids creation of duplicates
    private static final Map<Method, Consumer<Object>> handlerCache = new ConcurrentHashMap<>();

    // The source of the method
    private final Object source;

    // Wrapper for invoking the method
    private final Consumer<Object> handler;

    @SuppressWarnings("unchecked") // Will never fail
    public SubscribedMethod(Object source, Method method) {
        this.source = source;
        if (handlerCache.containsKey(method)) this.handler = handlerCache.get(method);
        else try {
            // Get lookup instance
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            // Check method modifiers for static
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            // Create methodtype for invoking the methodhandle
            MethodType targetSignature = MethodType.methodType(Consumer.class);
            // Generate callsite
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup, // The lookup instance to use
                    "accept", // The name of the method to implement
                    isStatic ? targetSignature : targetSignature.appendParameterTypes(source.getClass()), // The signature for .invoke()
                    MethodType.methodType(void.class, Object.class), // The method signature to implement
                    lookup.unreflect(method), // Method to invoke when called
                    MethodType.methodType(void.class, method.getParameterTypes()[0]) // Signature that is enforced at runtime
            );
            // Get target to invoke
            MethodHandle target = callSite.getTarget();
            // Invoke on the object if not static
            this.handler = (Consumer<Object>) (isStatic ? target.invoke() : target.invoke(source));
            // Cache this dynamic handler
            handlerCache.put(method, this.handler);
        } catch (Throwable throwable) {
            // This shouldn't ever happen
            throw new Error(throwable);
        }
    }

    /**
     * Gets the source of the method
     * @return The source of the method
     */
    public Object getSource() {
        return source;
    }

    public void invoke(Object event) {
        this.handler.accept(event);
    }
}
