/*
 * #%L
 * GraphWalker Example
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
package org.graphwalker.example;

import org.graphwalker.core.annotations.After;
import org.graphwalker.core.annotations.Before;
import org.graphwalker.core.annotations.GraphWalker;
import org.graphwalker.core.conditions.Any;
import org.junit.Assert;

@GraphWalker(id = "m1", model = "models/exampleModel.graphml")
public class ExampleImpl extends BaseClass {

    //TODO: Make a more interesting example
    @Before
    public void baseBefore() {

    }

    public void e_execute() {
        String property = System.getProperty("test.property");
        Assert.assertNotNull(property);
    }
    
    public void v_verify() {
    }
}
