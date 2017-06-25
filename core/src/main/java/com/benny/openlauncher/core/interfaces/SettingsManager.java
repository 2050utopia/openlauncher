package com.benny.openlauncher.core.interfaces;

import com.benny.openlauncher.core.widget.Desktop;

/**
 * Created by Michael on 25.06.2017.
 */

public interface SettingsManager {
    int getDesktopPageCurrent();
    void setDesktopPageCurrent(int page);
    boolean isDesktopShowIndicator();
    int getDesktopColumnCount();
    int getDesktopRowCount();
    int getDesktopStyle();
    boolean isDesktopShowLabel();
    int getDockSize();
    boolean getGestureDockSwipeUp();
    boolean isDesktopLock();
    void setDesktopLock(boolean locked);
    boolean isGestureFeedback();
    int getIconSize();
    boolean isDockShowLabel();
    int getDrawerColumnCount();
    int getDrawerRowCount();
    boolean isDrawerShowIndicator();
    int getDrawerStyle();
    boolean isDrawerShowCardView();
    int getDrawerCardColor();
    boolean isDrawerShowLabel();
    int getDrawerLabelColor();
    boolean isDrawerRememberPosition();
    boolean isDesktopFullscreen();
    int getDesktopColor();
    int getDockColor();
    int getDrawerBackgroundColor();
    boolean getDockEnable();
    boolean getSearchBarEnable();
    boolean getAppRestartRequired();
    void setAppRestartRequired(boolean required);
    boolean isAppFirstLaunch();
    void setAppFirstLaunch(boolean isAppFirstLaunch);
}
