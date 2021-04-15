/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sdwfqin.matisse.internal.entity;

import android.content.pm.ActivityInfo;

import androidx.annotation.StyleRes;

import com.github.sdwfqin.matisse.MimeType;
import com.github.sdwfqin.matisse.R;
import com.github.sdwfqin.matisse.engine.ImageEngine;
import com.github.sdwfqin.matisse.engine.impl.GlideEngine;
import com.github.sdwfqin.matisse.filter.Filter;
import com.github.sdwfqin.matisse.listener.OnCheckedListener;
import com.github.sdwfqin.matisse.listener.OnSelectedListener;

import java.util.List;
import java.util.Set;

public final class SelectionSpec {

    /**
     * 可选择的类型
     */
    public Set<MimeType> mimeTypeSet;
    /**
     * 在一次选择过程中，是否可以同时选择图像和视频。
     * true表示不能同时选择图像和视频，false表示可以同时选择图像和视频。
     */
    public boolean mediaTypeExclusive;
    public boolean showSingleMediaType;
    @StyleRes
    public int themeId;
    /**
     * 屏幕方向
     */
    public int orientation;
    /**
     * 当用户选择媒体时，显示一个自动增加的数字或一个复选标记。
     * 对于从1开始自动递增的数字，则为true；对于复选标记，为false。默认值为false。
     */
    public boolean countable;
    public int maxSelectable;
    public int maxImageSelectable;
    public int maxVideoSelectable;
    public List<Filter> filters;
    /**
     * 是否开启启用照片捕获。
     */
    public boolean capture;
    /**
     * 拍照的FileProvider权限
     */
    public CaptureStrategy captureStrategy;
    public int spanCount;
    public int gridExpectedSize;
    public float thumbnailScale;
    public ImageEngine imageEngine;
    public boolean hasInited;
    public OnSelectedListener onSelectedListener;
    /**
     * 原图选项
     */
    public boolean originalable;
    public boolean autoHideToobar;
    public int originalMaxSize;
    public OnCheckedListener onCheckedListener;
    public boolean showPreview;

    private SelectionSpec() {
    }

    public static SelectionSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static SelectionSpec getCleanInstance() {
        SelectionSpec selectionSpec = getInstance();
        selectionSpec.reset();
        return selectionSpec;
    }

    private void reset() {
        mimeTypeSet = null;
        mediaTypeExclusive = true;
        showSingleMediaType = false;
        themeId = R.style.Matisse_Zhihu;
        orientation = 0;
        countable = false;
        maxSelectable = 1;
        maxImageSelectable = 0;
        maxVideoSelectable = 0;
        filters = null;
        capture = false;
        captureStrategy = null;
        spanCount = 3;
        gridExpectedSize = 0;
        thumbnailScale = 0.5f;
        imageEngine = new GlideEngine();
        hasInited = true;
        originalable = false;
        autoHideToobar = false;
        originalMaxSize = Integer.MAX_VALUE;
        showPreview = true;
    }

    public boolean singleSelectionModeEnabled() {
        return !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1));
    }

    public boolean needOrientationRestriction() {
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public boolean onlyShowImages() {
        return showSingleMediaType && MimeType.ofImage().containsAll(mimeTypeSet);
    }

    public boolean onlyShowVideos() {
        return showSingleMediaType && MimeType.ofVideo().containsAll(mimeTypeSet);
    }

    public boolean onlyShowGif() {
        return showSingleMediaType && MimeType.ofGif().equals(mimeTypeSet);
    }

    private static final class InstanceHolder {
        private static final SelectionSpec INSTANCE = new SelectionSpec();
    }
}
