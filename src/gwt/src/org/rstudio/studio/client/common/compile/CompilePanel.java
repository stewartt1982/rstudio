/*
 * CompilePanel.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */


package org.rstudio.studio.client.common.compile;

import org.rstudio.core.client.CodeNavigationTarget;
import org.rstudio.core.client.events.HasSelectionCommitHandlers;
import org.rstudio.core.client.widget.LeftRightToggleButton;
import org.rstudio.core.client.widget.Toolbar;
import org.rstudio.core.client.widget.ToolbarButton;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.common.compile.errorlist.CompileErrorList;
import org.rstudio.studio.client.workbench.commands.Commands;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class CompilePanel extends Composite
{
   public CompilePanel()
   {
      panel_ = new SimplePanel();
      
      outputBuffer_ = new CompileOutputBuffer();
      panel_.setWidget(outputBuffer_);
      errorList_ = new CompileErrorList();
      
      initWidget(panel_);
   }
   
   public void connectToolbar(Toolbar toolbar)
   {
      Commands commands = RStudioGinjector.INSTANCE.getCommands();
      ImageResource stopImage = commands.interruptR().getImageResource();
      stopButton_ = new ToolbarButton(stopImage, null);
      stopButton_.setVisible(false);
      toolbar.addRightWidget(stopButton_);
      
      showOutputButton_ = new LeftRightToggleButton("Output", "Issues", false);
      showOutputButton_.setVisible(false);
      showOutputButton_.addClickHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event)
         {
           showOutputButton_.setVisible(false);
           showErrorsButton_.setVisible(true);
           panel_.setWidget(outputBuffer_);
           outputBuffer_.scrollToBottom();
         }
      });
      toolbar.addRightWidget(showOutputButton_);
       
      showErrorsButton_ = new LeftRightToggleButton("Output", "Issues",  true);
      showErrorsButton_.setVisible(false);
      showErrorsButton_.addClickHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event)
         {
           showOutputButton_.setVisible(true);
           showErrorsButton_.setVisible(false);
           panel_.setWidget(errorList_);
         }
      });
      toolbar.addRightWidget(showErrorsButton_);
   }
   
   public void compileStarted(String fileName)
   {
      clearAll();
      
      fileName_ = fileName;

      showOutputButton_.setVisible(false);
      showErrorsButton_.setVisible(false);
      stopButton_.setVisible(true);
   }

   public void clearAll()
   {
      fileName_ = null;
      showOutputButton_.setVisible(false);
      showErrorsButton_.setVisible(false);
      stopButton_.setVisible(false);
      outputBuffer_.clear();
      errorList_.clear();
      panel_.setWidget(outputBuffer_);  
   }
   
   public void showOutput(String output)
   {
      outputBuffer_.append(output);
   }
   
   public void showErrors(JsArray<CompileError> errors)
   {
      errorList_.showErrors(fileName_, errors);

      if (CompileError.includesErrorType(errors))
      {
         panel_.setWidget(errorList_);
         showOutputButton_.setVisible(true);
      }
      else
      {
         showErrorsButton_.setVisible(true);
      }
   }

   public boolean isErrorPanelShowing()
   {
      return errorList_.isAttached();
   }

 
   public void scrollToBottom()
   {
      outputBuffer_.scrollToBottom();
   }

   public void compileCompleted()
   {
      stopButton_.setVisible(false);
      
      if (isErrorPanelShowing())
      {
         errorList_.selectFirstItem();
         errorList_.focus();
      }
   }
   
   public HasClickHandlers stopButton()
   {
      return stopButton_;
   }
   
   public HasSelectionCommitHandlers<CodeNavigationTarget> errorList()
   {
      return errorList_;
   }
   

   private String fileName_;
   
   private ToolbarButton stopButton_;
   private LeftRightToggleButton showOutputButton_;
   private LeftRightToggleButton showErrorsButton_;
   private SimplePanel panel_;
   private CompileOutputBuffer outputBuffer_;
   private CompileErrorList errorList_;
}
