// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.shared.model.Language;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author jaimespacco
 *
 */
public class PlaygroundActionsPanel extends Composite
{
    /*
     * TODO: Clear button
     * new work button
     * load program button (some way to list these, probably
     *      in a popup panel?)
     * 
     */
    private Language language=Language.JAVA;
    private Runnable runHandler;
    private Runnable changeLanguageHandler;
    private ListBox selectLanguage;
    
    public void setSelectedLanguage(Language lang) {
        switch(lang) {
        case JAVA:
            selectLanguage.setSelectedIndex(1);
            break;
        case PYTHON:
            selectLanguage.setSelectedIndex(2);
            break;
        case C:
            selectLanguage.setSelectedIndex(3);
            break;
        case CPLUSPLUS:
            selectLanguage.setSelectedIndex(4);
            break;
        case RUBY:
            selectLanguage.setSelectedIndex(5);
            break;
        default:
            // default to Java (1)
            selectLanguage.setSelectedIndex(1);
        }
    }
    
    /**
     * Constructor.
     */
    public PlaygroundActionsPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        panel.addStyleName("paddedHorizontalPanel");
        //panel.setSpacing(20);
        
        selectLanguage = new ListBox();
        selectLanguage.addItem(Language.JAVA.toString());
        selectLanguage.addItem(Language.C.toString());
        selectLanguage.addItem(Language.CPLUSPLUS.toString());
        //selectLanguage.addItem(Language.PYTHON.toString());
        //selectLanguage.addItem(Language.RUBY.toString());
        //selectLanguage.addItem("OCaml (coming soon!)");
        // setting to 1 turns makes a drop-down list
        selectLanguage.setVisibleItemCount(1);
        selectLanguage.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int index=selectLanguage.getSelectedIndex();
                if (index==-1) {
                    language=null;
                }
                String lang=selectLanguage.getItemText(index);
                language=Language.valueOf(lang);
                if (changeLanguageHandler!=null) {
                    changeLanguageHandler.run();
                }
            }
        });
        
        // Change language selector
        panel.add(selectLanguage);

        // Run button
        Button runButton = new Button("Run!");
        runButton.setStylePrimaryName("cc-emphButton");
        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (runHandler != null) {
                    runHandler.run();
                }
            }
        });
        panel.add(runButton);
        
        initWidget(panel);
    }
    
    /**
     * Get the language currently set.
     * @return The language set.
     */
    public Language getLanguage() {
        return language;
    }
    
    /**
     * Set the handler to run when the Run! button is clicked.
     * 
     * @param submitHandler handler to run when the Run! button is clicked
     */
    public void setRunHandler(Runnable runHandler) {
        this.runHandler = runHandler;
    }
    
    public void setChangeLanguageHandler(Runnable changeLanguageHandler) {
        this.changeLanguageHandler=changeLanguageHandler;
    }
}
