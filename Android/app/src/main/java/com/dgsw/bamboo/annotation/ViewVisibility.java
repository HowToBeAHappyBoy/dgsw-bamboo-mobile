package com.dgsw.bamboo.annotation;

import android.support.annotation.IntDef;
import android.view.View;

@IntDef(value = {View.VISIBLE, View.INVISIBLE, View.GONE})
public @interface ViewVisibility {
}
