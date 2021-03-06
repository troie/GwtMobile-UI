/*
 * Copyright (c) 2010 Zhihua (Dennis) Jiang
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwtmobile.ui.client.page;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtmobile.ui.client.utils.Utils;

public abstract class Page extends Composite {

    private boolean _isInitialLoad = true;
    private Transition _transition;
    private static Transition _defaultTransition = Transition.SLIDE;
    
    @Override
    protected void initWidget(Widget widget) {
    	super.initWidget(widget);    	
		setStyleName("Page");    
    }
    
    @Override
    public void onLoad() {
        if (_isInitialLoad) {
            onInitialLoad();
            _isInitialLoad = false;
        }
    }
    
	protected void onInitialLoad() {
	}
	
	protected void onTransitionEnd() {
	    final Page to, from;
	    if (PageHistory.from() == null || PageHistory.from() != Page.this) {  //goto
	    	Utils.Console("goto");
	        from = PageHistory.current();
	        to = this;
	        PageHistory.add(to);
	        //TODO: change to use scheduler deferred command.
	        Timer timer = new Timer() {                
                @Override
                public void run() {
                    to.onNavigateTo();
                }
            };
            timer.schedule(1);
	    }
	    else {             //goback
	    	Utils.Console("goback");
	        from = PageHistory.current();
	        PageHistory.back();
	        to = PageHistory.current();
            Timer timer = new Timer() {                
                @Override
                public void run() {
                    to.onNavigateBack(from, PageHistory.getReturnValue());
                }
            };
            timer.schedule(1);
	    }       
	}
		
    protected void onNavigateTo() {
    }

    protected void onNavigateBack(Page from, Object object) {
    }

    public void goTo(final Page toPage, final Transition transition) {
	    final Page fromPage = this;
    	toPage.setTransition(transition);
    	if (transition != null) {
        	transition.start(fromPage, toPage, false);
    	}
    	else {
    		Transition.start(fromPage, toPage);
    	}
	}

	public void goBack(Object returnValue) {
        final Page fromPage = this;
        PageHistory.setReturnValue(returnValue);
        final Page toPage = PageHistory.from();
		if (toPage == null) {
			// exit app here.
		    return;
		}
		final Transition transition = fromPage.getTransition();
    	if (transition != null) {
    		transition.start(fromPage, toPage, true);
    	}
    	else {
    		Transition.start(fromPage, toPage);
    	}
	}
	
	void setTransition(Transition transition) {
		_transition = transition;
	}
	
	Transition getTransition() {
		return _transition;
	}

	public static void load(Page mainPage) {
		setPageResolution();
        RootLayoutPanel.get().add(mainPage);
        PageHistory.add(mainPage);
	}
	
	public static void setDefaultTransition(Transition transition) {
		_defaultTransition = transition;
	}

    public void goTo(final Page toPage) {
    	goTo(toPage, _defaultTransition);
	}
    
	private static void setPageResolution() {
		String ratio = getDevicePixelRatio();
		Utils.Console("Device Pixel Ratio: " + ratio);
		if (ratio.equals("1.5")) {
	    	Document.get().getDocumentElement().setClassName("WVGA");
		}
		else if (ratio.equals("0.75")) {
	    	Document.get().getDocumentElement().setClassName("QVGA");
		}
		else {
	    	Document.get().getDocumentElement().setClassName("HVGA");
		}
	}

    public static native String getDevicePixelRatio() /*-{
		return $wnd.devicePixelRatio + "";
	}-*/;

}
