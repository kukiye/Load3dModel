package com.kuki.load3dmodel.callback;


import com.kuki.load3dmodel.model.STLModel;

public interface OnReadListener {
    void onstart();

    void onLoading(int cur, int total);

    void onFinished(STLModel model);

    void onFailure(Exception e);
}