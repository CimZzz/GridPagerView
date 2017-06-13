package com.virtualightning.gridpagerview;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by CimZzz on 13/6/17.<br>
 * Project Name : Virtual-Lightning GridPagerView<br>
 * Since : GridPagerView_0.0.1<br>
 * Description:<br>
 * Description
 */
public abstract class GridPagerAdapter {
    GridPagerView pagerView;

    protected abstract int getItemCount();
    protected abstract ViewWrapper getChildView(Context context, ViewGroup container, ViewPool viewPool, int position);
    protected abstract Object getItemAt(int position);

    public final void notifyDataSetChanged() {
        pagerView.update();
    }
}
