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
package org.graphwalker.core.utils;

import org.graphwalker.core.Bundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Reflection {

    private Reflection() {
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new ReflectionException(Resource.getText(Bundle.NAME, "exception.class.instantiation", clazz.getName()));
        } catch (IllegalAccessException e) {
            throw new ReflectionException(Resource.getText(Bundle.NAME, "exception.class.instantiation", clazz.getName()));
        }
    }
    
    public static <T> T newInstance(Class<T> clazz, Object... arguments) {
        try {
            Constructor<T> constructor = clazz.getConstructor(getTypes(arguments));
            return constructor.newInstance(arguments);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(Resource.getText(Bundle.NAME, "exception.class.instantiation", clazz.getName()));
        } catch (InvocationTargetException e) {
            throw new ReflectionException(Resource.getText(Bundle.NAME, "exception.class.instantiation", clazz.getName()));
        } catch (InstantiationException e) {
            throw new ReflectionException(Resource.getText(Bundle.NAME, "exception.class.instantiation", clazz.getName()));
        } catch (IllegalAccessException e) {
            throw new ReflectionException(Resource.getText(Bundle.NAME, "exception.class.instantiation", clazz.getName()));
        }
    }
    
    private static Class<?>[] getTypes(Object... arguments) {
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (Object argument: arguments) {
            types.add(argument.getClass());
        }
        return types.toArray(new Class<?>[types.size()]);
    }
}
