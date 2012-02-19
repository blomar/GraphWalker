/*
 * #%L
 * GraphWalker GUI
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
package org.graphwalker.gui.actions;

import org.graphwalker.core.utils.Resource;
import org.graphwalker.gui.Bundle;
import org.graphwalker.gui.GraphWalkerView;

import java.awt.event.ActionEvent;

/**
 * <p>ExitAction class.</p>
 *
 * @author nilols
 * @version $Id: $
 */
public class ExitAction extends GraphWalkerAbstractAction {

    /**
     * <p>Constructor for ExitAction.</p>
     *
     * @param view a {@link org.graphwalker.gui.GraphWalkerView} object.
     */
    public ExitAction(GraphWalkerView view) {
        super(view);
        putValue(ActionConstants.GROUP, Resource.getText(Bundle.NAME, "menu.exit.group"));
        putValue(ActionConstants.INDEX, Integer.parseInt(Resource.getText(Bundle.NAME, "menu.exit.index")));
        putValue(ActionConstants.NAME, Resource.getText(Bundle.NAME, "menu.exit.label"));
        putValue(ActionConstants.DESCRIPTION, Resource.getText(Bundle.NAME, "menu.exit.description"));
        putValue(ActionConstants.ICON, Resource.getIcon(Bundle.NAME, "menu.exit.icon"));
    }
    
    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        System.exit(0); //TODO: Do we need to save anything?
    }
}