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
package org.graphwalker.core.filter;

import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EdgeFilterTest {

    private Edge createEdge() {
        Edge edge = new Edge("edge1");
        List<Action> actions = new ArrayList<Action>(); 
        actions.add(new Action("i = 0;"));
        edge.setEdgeActions(actions);
        edge.setEdgeGuard(new Guard("i == 0"));
        return edge;
    }
    
    @Test(expected = EdgeFilterException.class)
    public void unknownScriptEngine() {
        EdgeFilter edgeFilter = new EdgeFilter("unknown");
        edgeFilter.executeActions(null, createEdge());
    }

    @Test(expected = EdgeFilterException.class)
    public void groovyScriptEngineException() {
        EdgeFilter edgeFilter = new EdgeFilter("groovy");
        Assert.assertTrue(edgeFilter.acceptEdge(null, createEdge()));
    }
    
    @Test
    public void groovyScriptEngine() {
        EdgeFilter edgeFilter = new EdgeFilter("groovy");
        Edge edge = createEdge();
        edgeFilter.executeActions(null, edge);
        Assert.assertTrue(edgeFilter.acceptEdge(null, edge));
    }  
    
    @Test
    public void callImplementationMethod() {
        EdgeFilter edgeFilter = new EdgeFilter("groovy");
        Model model = new Model("m1");
        model.setImplementation(this);
        Edge edge = new Edge("edge1");
        edge.setEdgeGuard(new Guard("impl.not(false)"));
        Assert.assertTrue(edgeFilter.acceptEdge(model, edge));
    }

    public boolean not(boolean flag) {
        return !flag;
    }
}
