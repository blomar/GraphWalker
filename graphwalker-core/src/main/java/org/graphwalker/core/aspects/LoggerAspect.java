/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2012 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.graphwalker.core.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.graphwalker.core.Bundle;
import org.graphwalker.core.common.ResourceUtils;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.ModelElement;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;


/**
 * @author Nils Olsson
 */
@Aspect
public final class LoggerAspect {

    private final ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

    static {
        MDC.put("traceId", UUID.randomUUID().toString());
    }

    @Pointcut("execution(public * *(..))")
    void publicMethod() {
    }

    @Pointcut("within(org.graphwalker.core.GraphWalker+)")
    void graphWalker() {
    }

    /**
     * <p>graphWalkerAnnotation.</p>
     */
    @Pointcut("within(@org.graphwalker.core.annotations.GraphWalker *)")
    public void graphWalkerAnnotation() {
    }

    @Pointcut("within(org.graphwalker.core.machine.Machine+)")
    void machine() {
    }

    @Pointcut("within(org.graphwalker.core.script.EdgeFilter+)")
    void edgeFilter() {
    }

    @Pointcut("within(org.graphwalker.core.conditions.StopCondition+)")
    void stopCondition() {
    }

    @Pointcut("execution(* *.executeElement(..)) && args(context, element)")
    void executeElement(ExecutionContext context, ModelElement element) {
    }

    /**
     * <p>beforeExecuteElement.</p>
     *
     * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object.
     * @param element   a {@link org.graphwalker.core.model.ImmutableElement} object.
     */
    @Before("machine() && executeElement(context, element)")
    public void beforeExecuteElement(JoinPoint joinPoint, ExecutionContext context, ModelElement element) {
        Logger logger = getLogger(joinPoint);
        if (logger.isInfoEnabled()) {
            logger.info(ResourceUtils.getText(Bundle.NAME, "log.method.call"
                    , context.getModel().getId()
                    , (element instanceof Edge ? "edge" : "vertex")
                    , element.getId()
                    , element.getName()
                    , context.getVisitCount(element)));
        }
    }

    /**
     * <p>logInfoBefore.</p>
     *
     * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object.
     */
    @Before("publicMethod() && machine()")
    public void logInfoBefore(JoinPoint joinPoint) {
        MDC.put("traceId", UUID.randomUUID().toString());
        Logger logger = getLogger(joinPoint);
        if (logger.isDebugEnabled()) {
            logger.debug(toString(joinPoint));
        }
    }

    /**
     * <p>logImplementation.</p>
     *
     * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object.
     */
    @Before("graphWalkerAnnotation()")
    public void logImplementation(JoinPoint joinPoint) {
        Logger logger = getLogger(joinPoint);
        if (logger.isDebugEnabled()) {
            logger.debug(toString(joinPoint));
        }
    }

    /**
     * <p>logDebug.</p>
     *
     * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object.
     */
    @Before("publicMethod() && (machine() || edgeFilter() || stopCondition())")
    public void logDebug(JoinPoint joinPoint) {
        getLogger(joinPoint).debug(toString(joinPoint));
    }

    private Logger getLogger(JoinPoint joinPoint) {
        return loggerFactory.getLogger(joinPoint.getSignature().getDeclaringType().getSimpleName());
    }

    private Logger getLogger(Throwable throwable) {
        return loggerFactory.getLogger(throwable.getClass().getName());
    }

    private String toString(JoinPoint joinPoint) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(joinPoint.getTarget().getClass().getSimpleName());
        buffer.append(".");
        buffer.append(joinPoint.getSignature().getName());
        buffer.append("()");
        return buffer.toString();
    }

}
