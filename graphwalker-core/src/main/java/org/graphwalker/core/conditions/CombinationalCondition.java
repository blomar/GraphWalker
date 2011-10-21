/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 GraphWalker
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

import org.graphwalker.core.machines.FiniteStateMachine;

import java.util.Iterator;
import java.util.Vector;

/**
 * <p>CombinationalCondition class.</p>
 *
 * @author nilols
 * @version $Id: $
 */
public class CombinationalCondition extends AbstractStopCondition {

    private Vector<StopCondition> conditions;

    /** {@inheritDoc} */
    @Override
    public boolean isFulfilled() {
        for (StopCondition condition : conditions) {
            if (!condition.isFulfilled()) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Constructor for CombinationalCondition.</p>
     */
    public CombinationalCondition() {
        this.conditions = new Vector<StopCondition>();
    }

    /**
     * <p>add.</p>
     *
     * @param condition a {@link org.graphwalker.core.conditions.StopCondition} object.
     */
    public void add(StopCondition condition) {
        this.conditions.add(condition);
    }

    /** {@inheritDoc} */
    @Override
    public void setMachine(FiniteStateMachine machine) {
        super.setMachine(machine);
        for (StopCondition condition : conditions) {
            condition.setMachine(machine);
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getFulfilment() {
        double retur = 0;
        for (StopCondition condition : conditions) {
            retur += condition.getFulfilment();
        }
        return retur / conditions.size();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("(");
        for (Iterator<StopCondition> i = conditions.iterator(); i.hasNext(); ) {
            stringBuilder.append(i.next().toString());
            if (i.hasNext()) {
                stringBuilder.append(" AND ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

}
