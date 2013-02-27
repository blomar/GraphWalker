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
package org.graphwalker.core.conditions;

import org.graphwalker.core.GraphWalker;
import org.graphwalker.core.GraphWalkerFactory;
import org.graphwalker.core.configuration.Configuration;
import org.graphwalker.core.generators.PathGeneratorFactory;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;

public class TimeDurationTest {

    private Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        Model model = configuration.addModel(new Model("m1"));
        Vertex v_start = model.addVertex(new Vertex("Start"));
        Vertex v_1 = model.addVertex(new Vertex("v_1"));
        model.addEdge(new Edge(), v_start, v_1);
        model.addEdge(new Edge("e_1"), v_1, v_1);
        model.setPathGenerator(PathGeneratorFactory.create("Random"));
        model.getPathGenerator().setStopCondition(StopConditionFactory.create("TimeDuration", 3));
        return configuration;
    }

    //@Test
    public void testExecute() {
        GraphWalker graphWalker = GraphWalkerFactory.create(createConfiguration());
        long start = System.currentTimeMillis();
        while (graphWalker.hasNextStep()) {
            graphWalker.getNextStep();
        }
        long stop = System.currentTimeMillis();
        Assert.assertEquals(3000, stop-start, 10);
    }
}
