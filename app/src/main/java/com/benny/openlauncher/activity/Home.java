package com.benny.openlauncher.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.core.interfaces.AppDeleteListener;
import com.benny.openlauncher.core.interfaces.AppItemView;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.DatabaseHelper;
import com.benny.openlauncher.core.interfaces.DialogHandler;
import com.benny.openlauncher.core.interfaces.Item;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.manager.StaticSetup;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.core.widget.GroupPopupView;
import com.benny.openlauncher.model.AppItem;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.viewutil.GroupIconDrawable;
import com.benny.openlauncher.viewutil.IconListAdapter;
import com.benny.openlauncher.viewutil.ItemViewFactory;
import com.benny.openlauncher.viewutil.QuickCenterItem;
import com.benny.openlauncher.widget.MiniPopupView;
import com.benny.openlauncher.widget.SearchBar;
import com.benny.openlauncher.widget.SwipeListView;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class Home extends com.benny.openlauncher.core.activity.Home implements DrawerLayout.DrawerListener
{
    private Unbinder unbinder;

    @BindView(R.id.groupPopup)
    public GroupPopupView groupPopup;
    @BindView(R.id.minibar)
    public SwipeListView minibar;
    @BindView(R.id.minibar_background)
    public FrameLayout minibarBackground;
    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;
    @BindView(R.id.miniPopup)
    public MiniPopupView miniPopup;
    @BindView(R.id.shortcutLayout)
    public RelativeLayout shortcutLayout;
    private FastItemAdapter<QuickCenterItem.ContactItem> quickContactFA;
    private CallLogObserver callLogObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomActivityOnCrash.setShowErrorDetails(true);
        CustomActivityOnCrash.setEnableAppRestart(false);
        CustomActivityOnCrash.setDefaultErrorActivityDrawable(R.drawable.rip);
        CustomActivityOnCrash.install(this);
    }

    @Override
    protected void bindViews()
    {
        super.bindViews();
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void unbindViews()
    {
        super.unbindViews();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    protected void initAppManager() {
        super.initAppManager();
        AppManager.getInstance(this).init();
    }

    @Override
    protected void initViews() {
        super.initViews();

        initMinibar();
        initQuickCenter();
    }

    @Override
    protected TextView getSearchClock() {
        return ((SearchBar)searchBar).searchClock;
    }

    private AppSettings getAppSettings() {
        return (AppSettings)appSettings;
    }

    protected void initSettings() {
        super.initSettings();
        drawerLayout.setDrawerLockMode(getAppSettings().getMinibarEnable() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void initMinibar() {
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> icons = new ArrayList<>();

        for (String act : getAppSettings().getMinibarArrangement()) {
            if (act.length() > 1 && act.charAt(0) == '0') {
                LauncherAction.ActionDisplayItem item = LauncherAction.getActionItemFromString(act.substring(1));
                if (item != null) {
                    labels.add(item.label.toString());
                    icons.add(item.icon);
                }
            }
        }

        minibar.setAdapter(new IconListAdapter(this, labels, icons));
        minibar.setOnSwipeRight(new SwipeListView.OnSwipeRight() {
            @Override
            public void onSwipe(int pos, float x, float y) {
                miniPopup.showActionWindow(LauncherAction.Action.valueOf(labels.get(pos)), x, y + (shortcutLayout.getHeight() - minibar.getHeight()) / 2);
            }
        });
        minibar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LauncherAction.Action action = LauncherAction.Action.valueOf(labels.get(i));
                if (action == LauncherAction.Action.DeviceSettings || action == LauncherAction.Action.LauncherSettings || action == LauncherAction.Action.EditMinBar) {
                    consumeNextResume = true;
                }
                LauncherAction.RunAction(action, Home.this);
                if (action != LauncherAction.Action.DeviceSettings && action != LauncherAction.Action.LauncherSettings && action != LauncherAction.Action.EditMinBar) {
                    drawerLayout.closeDrawers();
                }
            }
        });
        minibar.setBackgroundColor(getAppSettings().getMinibarBackgroundColor());
        minibarBackground.setBackgroundColor(getAppSettings().getMinibarBackgroundColor());
    }

    private void initQuickCenter() {
        RecyclerView quickContact = (RecyclerView) findViewById(R.id.quickContactRv);
        quickContact.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quickContactFA = new FastItemAdapter<>();
        quickContact.setAdapter(quickContactFA);

        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            callLogObserver = new CallLogObserver(new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);

            // get the call history for the adapter
            callLogObserver.onChange(true);
        } else {
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_PERMISSION_READ_CALL_LOG);
        }
    }

    protected void initSearchBar() {
        ((SearchBar)searchBar).setCallback(new SearchBar.CallBack() {
            @Override
            public void onInternetSearch(String string) {
                Intent intent = new Intent();

                if (Tool.isIntentActionAvailable(getApplicationContext(), Intent.ACTION_WEB_SEARCH)) {
                    intent.setAction(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, string);
                } else {
                    String baseUri = getAppSettings().getSearchBarBaseURI();
                    String searchUri = baseUri.contains("{query}") ? baseUri.replace("{query}", string) : (baseUri + string);

                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(searchUri));
                }

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onExpand() {
                Tool.invisibleViews(desktop, desktopIndicator);
                Tool.visibleViews(background);

                updateDock(false);

                ((SearchBar)searchBar).searchInput.setFocusable(true);
                ((SearchBar)searchBar).searchInput.setFocusableInTouchMode(true);
                ((SearchBar)searchBar).searchInput.post(new Runnable() {
                    @Override
                    public void run() {
                        ((SearchBar)searchBar).searchInput.requestFocus();
                    }
                });

                Tool.showKeyboard(Home.this, ((SearchBar)searchBar).searchInput);
            }

            @Override
            public void onCollapse() {
                Tool.visibleViews(desktop, desktopIndicator);
                Tool.invisibleViews(background);

                updateDock(true);

                ((SearchBar)searchBar).searchInput.clearFocus();

                Tool.hideKeyboard(Home.this, ((SearchBar)searchBar).searchInput);
            }
        });

        super.initSearchBar();
    }

    public void updateHomeLayout() {
        updateSearchBar(true);
        updateDock(true);

        if (!appSettings.isDesktopShowIndicator()) {
            Tool.goneViews(100, desktopIndicator);
        }

        if (appSettings.getSearchBarEnable()) {
            ((ViewGroup.MarginLayoutParams) dragLeft.getLayoutParams()).topMargin = Desktop.topInset;
            ((ViewGroup.MarginLayoutParams) dragRight.getLayoutParams()).topMargin = Desktop.topInset;
        } else {
            desktop.setPadding(0, Desktop.topInset, 0, 0);
        }

        if (!appSettings.getDockEnable()) {
            desktop.setPadding(0, 0, 0, Desktop.bottomInset);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // check if reading the call log is permitted
        if (requestCode == REQUEST_PERMISSION_READ_CALL_LOG && callLogObserver != null) {
            callLogObserver = new CallLogObserver(new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);

            // get the call history for the adapter
            callLogObserver.onChange(true);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (callLogObserver != null)
            getApplicationContext().getContentResolver().unregisterContentObserver(callLogObserver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        drawerLayout.closeDrawers();
        super.onBackPressed();
    }

    // search button in the search bar is clicked
    public void onSearch(View view) {
        Intent i;
        try {
            i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            i = new Intent(Intent.ACTION_WEB_SEARCH);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        Home.this.startActivity(i);
    }

    // voice button in the search bar clicked
    public void onVoiceSearch(View view) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            Tool.toast(Home.this, "Can not find google search app");
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        switch (newState) {
            case DrawerLayout.STATE_DRAGGING:
            case DrawerLayout.STATE_SETTLING:
                if (shortcutLayout.getAlpha() == 1)
                    shortcutLayout.animate().setDuration(180L).alpha(0).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            shortcutLayout.setVisibility(View.INVISIBLE);
                        }
                    });
                break;
            case DrawerLayout.STATE_IDLE:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    shortcutLayout.setVisibility(View.VISIBLE);
                    shortcutLayout.setAlpha(0);
                    shortcutLayout.animate().setDuration(180L).alpha(1).setInterpolator(new AccelerateDecelerateInterpolator());
                }
                break;
        }
    }

    @Override
    protected void onHandleLauncherPause()
    {
        ((SearchBar)searchBar).collapse();
        super.onHandleLauncherPause();
    }

    @Override
    protected void initStaticHelper()
    {
        final DialogHandler dialogHandler = new DialogHandler() {
            @Override
            public void showPickAction(Context context, final IOnAddAppDrawerItem resultHandler) {
                DialogHelper.addActionItemDialog(context, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                        switch (which) {
                            case 0:
                                resultHandler.onAdd();
                                break;
                        }
                    }
                });
            }

            @Override
            public void showEditDialog(Context context, final Item item, IOnEditDialog resultHandler) {
                DialogHelper.editItemDialog("Edit Item", item.getLabel(), context, new DialogHelper.onItemEditListener() {
                    @Override
                    public void itemLabel(String label) {
                        item.setLabel(label);
                        Home.db.updateItem(item);

                        Home.launcher.desktop.addItemToCell(item, item.getX(), item.getY());
                        Home.launcher.desktop.removeItem(Home.launcher.desktop.getCurrentPage().coordinateToChildView(new Point(item.getX(), item.getY())));
                    }
                });
            }

            @Override
            public void showDeletePackageDialog(Context context, DragEvent dragEvent) {
                DialogHelper.deletePackageDialog(context, dragEvent);
            }
        };
        StaticSetup.init(new StaticSetup<Home, AppManager.App, com.benny.openlauncher.model.Item, AppItem, com.benny.openlauncher.widget.AppItemView>() {
            @Override
            public Class getItemClass() {
                return com.benny.openlauncher.model.Item.class;
            }

            @Override
            public SettingsManager getAppSettings() {
                return AppSettings.get();
            }

            @Override
            public DatabaseHelper createDatabaseHelper(Context context) {
                return new com.benny.openlauncher.util.DatabaseHelper(context);
            }

            @Override
            public List<AppManager.App> getAllApps(Context context) {
                return AppManager.getInstance(context).getApps();
            }

            @Override
            public List<AppItem> createAllAppItems(Context context) {
                List<AppItem> items = new ArrayList<AppItem>();
                List<AppManager.App> apps = getAllApps(context);
                for (AppManager.App app : apps)
                    items.add(createAppItem(app));
                return items;
            }

            @Override
            public AppItem createAppItem(AppManager.App app) {
                return new AppItem(app);
            }

            @Override
            public View createAppItemView(Context context, final Home home, AppManager.App app, AppItemView.LongPressCallBack longPressCallBack) {
                return new com.benny.openlauncher.widget.AppItemView.Builder(context)
                        .setAppItem(app)
                        .withOnTouchGetPosition()
                        .withOnLongClick(app, DragAction.Action.APP_DRAWER, new AppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return AppSettings.get().getDesktopStyle() != Desktop.DesktopMode.SHOW_ALL_APPS;
                            }

                            @Override
                            public void afterDrag(View view) {
                                home.closeAppDrawer();
                            }
                        })
                        .setLabelVisibility(AppSettings.get().isDrawerShowLabel())
                        .setTextColor(AppSettings.get().getDrawerLabelColor())
                        .getView();
            }

            @Override
            public AppItemView createAppItemViewPopup(Context context, com.benny.openlauncher.model.Item groupItem, AppManager.App item) {
                com.benny.openlauncher.widget.AppItemView.Builder b = new com.benny.openlauncher.widget.AppItemView.Builder(context).withOnTouchGetPosition();
                b.setTextColor(AppSettings.get().getDrawerLabelColor());
                if (groupItem.type == com.benny.openlauncher.model.Item.Type.SHORTCUT) {
                    b.setShortcutItem(groupItem);
                } else {
                    AppManager.App app = AppManager.getInstance(context).findApp(groupItem.intent);
                    if (app != null) {
                        b.setAppItem(groupItem, app);
                    }
                }
                final com.benny.openlauncher.widget.AppItemView view = b.getView();
                return view;
            }

            @Override
            public com.benny.openlauncher.model.Item newGroupItem() {
                return com.benny.openlauncher.model.Item.newGroupItem();
            }

            @Override
            public com.benny.openlauncher.model.Item newWidgetItem(int appWidgetId) {
                return com.benny.openlauncher.model.Item.newWidgetItem(appWidgetId);
            }

            @Override
            public com.benny.openlauncher.model.Item newActionItem(int action) {
                return com.benny.openlauncher.model.Item.newActionItem(action);
            }

            @Override
            public View getItemView(Context context, SettingsManager appSettings, com.benny.openlauncher.model.Item item, DesktopCallBack callBack) {
                int flag = appSettings.isDesktopShowLabel() ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
                View itemView = ItemViewFactory.getItemView(context, callBack, item, flag);
                return itemView;
            }

            @Override
            public SimpleFingerGestures.OnFingerGestureListener getDesktopGestureListener(final Desktop desktop) {
                return new SimpleFingerGestures.OnFingerGestureListener() {
                    @Override
                    public boolean onSwipeUp(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((com.benny.openlauncher.util.DatabaseHelper)Home.db).getGesture(1);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onSwipeDown(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((com.benny.openlauncher.util.DatabaseHelper)Home.db).getGesture(2);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onSwipeLeft(int i, long l, double v) {
                        return false;
                    }

                    @Override
                    public boolean onSwipeRight(int i, long l, double v) {
                        return false;
                    }

                    @Override
                    public boolean onPinch(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((com.benny.openlauncher.util.DatabaseHelper)Home.db).getGesture(3);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onUnpinch(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((com.benny.openlauncher.util.DatabaseHelper)Home.db).getGesture(4);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(int i) {
                        LauncherAction.ActionItem gesture = ((com.benny.openlauncher.util.DatabaseHelper)Home.db).getGesture(0);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }
                };
            }

            @Override
            public com.benny.openlauncher.model.Item createShortcut(Intent intent, Drawable icon, String name) {
                return com.benny.openlauncher.model.Item.newShortcutItem(intent, icon, name);
            }

            @Override
            public void showLauncherSettings(Context context) {
                LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            }

            @Override
            public DialogHandler getDialogHandler() {
                return dialogHandler;
            }

            @Override
            public void addAppUpdatedListener(Context c, AppUpdateListener listener) {
                AppManager.getInstance(c).addAppUpdatedListener(listener);
            }

            @Override
            public void removeAppUpdatedListener(Context c, AppUpdateListener<AppManager.App> listener) {
                AppManager.getInstance(c).removeAppUpdatedListener(listener);
            }

            @Override
            public void addAppDeletedListener(Context c, AppDeleteListener<AppManager.App> listener) {
                AppManager.getInstance(c).addAppDeletedListener(listener);
            }

            @Override
            public void onAppUpdated(Context p1, Intent p2) {
                AppManager.getInstance(p1).onReceive(p1, p2);
            }

            @Override
            public AppManager.App findApp(Context c, Intent intent) {
                return AppManager.getInstance(c).findApp(intent);
            }

            @Override
            public void updateIcon(Context context, com.benny.openlauncher.widget.AppItemView currentView, com.benny.openlauncher.model.Item currentItem) {
                currentView.setIcon(new GroupIconDrawable(context, currentItem));
            }

            @Override
            public void onItemViewDismissed(AppItemView itemView) {
                if (itemView.getIcon() instanceof GroupIconDrawable) {
                    ((GroupIconDrawable) itemView.getIcon()).popBack();
                }
            }
        });
    }

    public class CallLogObserver extends ContentObserver {

        private final String columns[] = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME};

        public CallLogObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        public void logCallLog() {
            if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                Tool.print("Manifest.permission.READ_CALL_LOG : PERMISSION_DENIED");
                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_PERMISSION_READ_CALL_LOG);
            } else {
                Cursor c = managedQuery(CallLog.Calls.CONTENT_URI, columns, null, null, CallLog.Calls.DATE + " DESC LIMIT 15");
                int number = c.getColumnIndex(CallLog.Calls.NUMBER);
                int name = c.getColumnIndex(CallLog.Calls.CACHED_NAME);

                Tool.print("Manifest.permission.READ_CALL_LOG : PERMISSION_GRANTED");
                quickContactFA.clear();
                while (c.moveToNext()) {
                    String phone = c.getString(number);
                    String uri = "tel:" + phone;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    String caller = c.getString(name);
                    quickContactFA.add(new QuickCenterItem.ContactItem(
                            new QuickCenterItem.ContactContent(caller, phone, intent,
                                    Tool.fetchThumbnail(Home.this, phone))));
                }
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            logCallLog();
        }
    }
}
