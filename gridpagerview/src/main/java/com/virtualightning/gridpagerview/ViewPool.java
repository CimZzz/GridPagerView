package com.virtualightning.gridpagerview;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by CimZzz on 13/6/17.<br>
 * Project Name : Virtual-Lightning GridPagerView<br>
 * Since : GridPagerView_0.0.1<br>
 * Description:<br>
 * Description
 */
public class ViewPool {
    private HashMap<Integer,LinkedList<ViewWrapper>> pool;

    ViewPool() {
        pool = new HashMap<>();
    }

    ViewWrapper getCacheView(int type) {
        LinkedList<ViewWrapper> cacheList = pool.get(type);
        if(cacheList != null && cacheList.size() != 0)
            return cacheList.poll();

        return null;
    }

    void cacheView (ViewWrapper view) {
        LinkedList<ViewWrapper> cacheList = pool.get(view.type);
        if(cacheList == null)
            pool.put(view.getType(),cacheList = new LinkedList<>());

        cacheList.push(view);
    }

    void clear() {
        pool.clear();
    }
}
