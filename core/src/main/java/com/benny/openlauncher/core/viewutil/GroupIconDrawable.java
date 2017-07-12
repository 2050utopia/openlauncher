package com.benny.openlauncher.core.viewutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.IconDrawer;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.Tool;

public class GroupIconDrawable extends Drawable implements IconDrawer {

    private int outlinepad;
    Drawable[] icons;
    public float iconSize;
    Paint paint;
    Paint paint2;
    Paint paint4;
    private int iconSizeDiv2;
    private float padding;

    private float scaleFactor = 1;

    private boolean needAnimate, needAnimateScale;

    private float sx = 1;
    private float sy = 1;

    public GroupIconDrawable(Context context, Item item, int iconSize) {
        final float size = Tool.dp2px(iconSize, context);
        final Drawable[] icons = new Drawable[4];
        for (int i = 0; i < 4; i++) {
            icons[i] = null;
        }
        init(icons, size);
        for (int i = 0; i < 4 && i < item.items.size(); i++) {
            App app = Setup.appLoader().findItemApp(item.items.get(i));
            if (app == null) {
                Setup.logger().log(this, Log.DEBUG, null, "Item %s has a null app at index %d (Intent: %s)", item.getLabel(), i, item.items.get(i).getIntent());
                icons[i] = new ColorDrawable(Color.TRANSPARENT);
            } else {
                app.getIconProvider().loadIconIntoIconDrawer(this, (int) size, i);
            }
        }
    }

    private void init(Drawable[] icons, float size) {
        this.icons = icons;
        this.iconSize = size;
        iconSizeDiv2 = Math.round(iconSize / 2f);
        padding = iconSize / 25f;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        paint.setAntiAlias(true);

        this.paint4 = new Paint();
        paint4.setColor(Color.WHITE);
        paint4.setAntiAlias(true);
        paint4.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint4.setStyle(Paint.Style.STROKE);
        outlinepad = Tool.dp2px(2, Home.launcher);
        paint4.setStrokeWidth(outlinepad);

        this.paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setFilterBitmap(true);
    }

    public void popUp() {
        sy = 1;
        sx = 1;
        needAnimate = true;
        needAnimateScale = true;
        invalidateSelf();
    }

    public void popBack() {
        needAnimate = false;
        needAnimateScale = false;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        if (needAnimateScale) {
            scaleFactor = Tool.clampFloat(scaleFactor - 0.09f, 0.5f, 1f);
        } else {
            scaleFactor = Tool.clampFloat(scaleFactor + 0.09f, 0.5f, 1f);
        }

        canvas.scale(scaleFactor, scaleFactor, iconSize / 2, iconSize / 2);

        Path clipp = new Path();
        clipp.addCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outlinepad, Path.Direction.CW);
        canvas.clipPath(clipp, Region.Op.REPLACE);

        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outlinepad, paint);

        if (icons[0] != null) {
            drawIcon(canvas, icons[0], padding, padding, iconSizeDiv2 - padding, iconSizeDiv2 - padding, paint2);
        }
        if (icons[1] != null) {
            drawIcon(canvas, icons[1], iconSizeDiv2 + padding, padding, iconSize - padding, iconSizeDiv2 - padding, paint2);
        }
        if (icons[2] != null) {
            drawIcon(canvas, icons[2], padding, iconSizeDiv2 + padding, iconSizeDiv2 - padding, iconSize - padding, paint2);
        }
        if (icons[3] != null) {
            drawIcon(canvas, icons[3], iconSizeDiv2 + padding, iconSizeDiv2 + padding, iconSize - padding, iconSize - padding, paint2);
        }
        canvas.clipRect(0, 0, iconSize, iconSize, Region.Op.REPLACE);

        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outlinepad, paint4);
        canvas.restore();

        if (needAnimate) {
            paint2.setAlpha(Tool.clampInt(paint2.getAlpha() - 25, 0, 255));
            invalidateSelf();
        } else if (paint2.getAlpha() != 255) {
            paint2.setAlpha(Tool.clampInt(paint2.getAlpha() + 25, 0, 255));
            invalidateSelf();
        }
    }

    private void drawIcon(Canvas canvas, Drawable icon, float l, float t, float r, float b, Paint paint) {
        icon.setBounds((int)l, (int)t, (int)r, (int)b);
        icon.setFilterBitmap(true);
        icon.setAlpha(paint.getAlpha());
        icon.draw(canvas);
//        canvas.drawBitmap(icon, null, new RectF(l, t, r, b), paint);
    }

    @Override
    public void setAlpha(int i) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void onIconAvailable(Drawable drawable, int index) {
        icons[index] = drawable;
        invalidateSelf();
    }

    @Override
    public void onIconCleared(Drawable placeholder, int index) {
        icons[index] = placeholder == null ? new ColorDrawable(Color.TRANSPARENT) : placeholder;
        invalidateSelf();
    }
}
