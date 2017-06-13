package com.virtualightning.gridpagerview;

import android.view.View;

/**
 * Created by CimZzz on 13/6/17.<br>
 * Project Name : Virtual-Lightning GridPagerView<br>
 * Since : GridPagerView_0.0.1<br>
 * Description:<br>
 * Description
 */
public class ViewWrapper {
    public final int type;
    public final View rootView;

    public ViewWrapper(int type, View rootView) {
        this.type = type;
        this.rootView = rootView;
        this.rootView.setTag(GridPagerView.WRAPPER_KEY,this);
    }

    public int getType() {
        return type;
    }
}
