// Generated by GraphWalker (http://www.graphwalker.org)                                
package org.graphwalker.example.models;

import org.graphwalker.core.annotations.Model;
import org.graphwalker.core.machine.ExecutionContext;

@Model(file = "org/graphwalker/example/models/ShoppingCart.graphml", type = "graphml")
public interface ShoppingCart {

    void e_ShoppingCart(ExecutionContext executionContext);

    void v_BrowserStarted(ExecutionContext executionContext);

    void Start(ExecutionContext executionContext);

    void v_OtherBoughtBooks(ExecutionContext executionContext);

    void e_EnterBaseURL(ExecutionContext executionContext);

    void e_AddBookToCart(ExecutionContext executionContext);

    void e_SearchBook(ExecutionContext executionContext);

    void e_StartBrowser(ExecutionContext executionContext);

    void v_BookInformation(ExecutionContext executionContext);

    void v_ShoppingCart(ExecutionContext executionContext);

    void v_SearchResult(ExecutionContext executionContext);

    void v_BaseURL(ExecutionContext executionContext);

    void e_ClickBook(ExecutionContext executionContext);
}