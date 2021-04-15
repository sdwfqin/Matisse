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
package com.github.sdwfqin.matisse;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;

import com.github.sdwfqin.matisse.engine.impl.GlideEngine;
import com.github.sdwfqin.matisse.engine.impl.PicassoEngine;
import com.github.sdwfqin.matisse.filter.Filter;
import com.github.sdwfqin.matisse.engine.ImageEngine;
import com.github.sdwfqin.matisse.internal.entity.CaptureStrategy;
import com.github.sdwfqin.matisse.internal.entity.SelectionSpec;
import com.github.sdwfqin.matisse.listener.OnCheckedListener;
import com.github.sdwfqin.matisse.listener.OnSelectedListener;
import com.github.sdwfqin.matisse.ui.MatisseActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Set;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

/**
 * Fluent API for building media select specification.
 */
@SuppressWarnings("unused")
public final class SelectionCreator {
    private final Matisse mMatisse;
    private final SelectionSpec mSelectionSpec;

    /**
     * Constructs a new specification builder on the context.
     *
     * @param matisse   a requester context wrapper.
     * @param mimeTypes MIME type set to select.
     */
    SelectionCreator(Matisse matisse, @NonNull Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
        mMatisse = matisse;
        mSelectionSpec = SelectionSpec.getCleanInstance();
        mSelectionSpec.mimeTypeSet = mimeTypes;
        mSelectionSpec.mediaTypeExclusive = mediaTypeExclusive;
        mSelectionSpec.orientation = SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * 如果选择的媒体仅是图像或视频，则仅显示一种媒体类型。
     *
     * @param showSingleMediaType 是否仅显示一种媒体类型，即图像还是视频。
     * @return {@link SelectionCreator} for fluent API.
     * @see SelectionSpec#onlyShowImages()
     * @see SelectionSpec#onlyShowVideos()
     */
    public SelectionCreator showSingleMediaType(boolean showSingleMediaType) {
        mSelectionSpec.showSingleMediaType = showSingleMediaType;
        return this;
    }

    /**
     * Theme for media selecting Activity.
     * <p>
     * There are two built-in themes:
     * 1. com.zhihu.matisse.R.style.Matisse_Zhihu;
     * 2. com.zhihu.matisse.R.style.Matisse_Dracula
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeId theme resource id. Default value is com.zhihu.matisse.R.style.Matisse_Zhihu.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator theme(@StyleRes int themeId) {
        mSelectionSpec.themeId = themeId;
        return this;
    }

    /**
     * 当用户选择媒体时，显示一个自动增加的数字或一个复选标记。
     *
     * @param countable 对于从1开始自动递增的数字，则为true；对于复选标记，为false。默认值为false。
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator countable(boolean countable) {
        mSelectionSpec.countable = countable;
        return this;
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator maxSelectable(int maxSelectable) {
        if (maxSelectable < 1) {
            throw new IllegalArgumentException("maxSelectable must be greater than or equal to one");
        }
        if (mSelectionSpec.maxImageSelectable > 0 || mSelectionSpec.maxVideoSelectable > 0) {
            throw new IllegalStateException("already set maxImageSelectable and maxVideoSelectable");
        }
        mSelectionSpec.maxSelectable = maxSelectable;
        return this;
    }

    /**
     * Only useful when {@link SelectionSpec#mediaTypeExclusive} set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable) {
        if (maxImageSelectable < 1 || maxVideoSelectable < 1) {
            throw new IllegalArgumentException(("max selectable must be greater than or equal to one"));
        }
        mSelectionSpec.maxSelectable = -1;
        mSelectionSpec.maxImageSelectable = maxImageSelectable;
        mSelectionSpec.maxVideoSelectable = maxVideoSelectable;
        return this;
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param filter {@link Filter}
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator addFilter(@NonNull Filter filter) {
        if (mSelectionSpec.filters == null) {
            mSelectionSpec.filters = new ArrayList<>();
        }
        if (filter == null) throw new IllegalArgumentException("filter cannot be null");
        mSelectionSpec.filters.add(filter);
        return this;
    }

    /**
     * 是否开启启用照片捕获。
     * <p>
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator capture(boolean enable) {
        mSelectionSpec.capture = enable;
        return this;
    }

    /**
     * 显示原始照片检查选项。让用户决定选择后是否使用原始照片（原图选项）
     *
     * @param enable Whether to enable original photo or not
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator originalEnable(boolean enable) {
        mSelectionSpec.originalable = enable;
        return this;
    }

    /**
     * 用户单击图片时是否在“预览”模式下隐藏顶部和底部工具栏
     *
     * @param enable
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator autoHideToolbarOnSingleTap(boolean enable) {
        mSelectionSpec.autoHideToobar = enable;
        return this;
    }

    /**
     * 原图大小上限，单位Mb，只有在originalEnable=true时生效
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator maxOriginalSize(int size) {
        mSelectionSpec.originalMaxSize = size;
        return this;
    }

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for {@link androidx.core.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, needed only when capturing is enabled.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator captureStrategy(CaptureStrategy captureStrategy) {
        mSelectionSpec.captureStrategy = captureStrategy;
        return this;
    }

    /**
     * activity屏幕方向
     *
     * @param orientation An orientation constant as used in {@link ScreenOrientation}.
     *                    Default value is {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}.
     * @return {@link SelectionCreator} for fluent API.
     * @see Activity#setRequestedOrientation(int)
     */
    public SelectionCreator restrictOrientation(@ScreenOrientation int orientation) {
        mSelectionSpec.orientation = orientation;
        return this;
    }

    /**
     * Set a fixed span count for the media grid. Same for different screen orientations.
     * <p>
     * This will be ignored when {@link #gridExpectedSize(int)} is set.
     *
     * @param spanCount Requested span count.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator spanCount(int spanCount) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("spanCount cannot be less than 1");
        }
        mSelectionSpec.spanCount = spanCount;
        return this;
    }

    /**
     * 设置媒体网格的预期大小以适应不同的屏幕大小。由于媒体网格应填充视图容器，因此不一定会应用此方法。
     * 所测量的媒体网格的大小将尽可能接近此值。
     *
     * @param size 预期的媒体网格大小（以像素为单位）。
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator gridExpectedSize(int size) {
        mSelectionSpec.gridExpectedSize = size;
        return this;
    }

    /**
     * 照片缩略图的比例与视图的尺寸相比。它应该是[0.0，1.0]中的浮点值。
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f) {
            throw new IllegalArgumentException("Thumbnail scale must be between (0.0, 1.0]");
        }
        mSelectionSpec.thumbnailScale = scale;
        return this;
    }

    /**
     * 图像展示引擎
     * <p>
     * There are two built-in image engines:
     * 1. {@link GlideEngine}
     * 2. {@link PicassoEngine}
     * And you can implement your own image engine.
     *
     * @param imageEngine {@link ImageEngine}
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator imageEngine(ImageEngine imageEngine) {
        mSelectionSpec.imageEngine = imageEngine;
        return this;
    }

    /**
     * Set listener for callback immediately when user select or unselect something.
     * <p>
     * It's a redundant API with {@link Matisse#obtainResult(Intent)},
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link SelectionCreator} for fluent API.
     */
    @NonNull
    public SelectionCreator setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mSelectionSpec.onSelectedListener = listener;
        return this;
    }

    /**
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mSelectionSpec.onCheckedListener = listener;
        return this;
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    public void forResult(int requestCode) {
        Activity activity = mMatisse.getActivity();
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, MatisseActivity.class);

        Fragment fragment = mMatisse.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public SelectionCreator showPreview(boolean showPreview) {
        mSelectionSpec.showPreview = showPreview;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @IntDef({
            SCREEN_ORIENTATION_UNSPECIFIED,
            SCREEN_ORIENTATION_LANDSCAPE,
            SCREEN_ORIENTATION_PORTRAIT,
            SCREEN_ORIENTATION_USER,
            SCREEN_ORIENTATION_BEHIND,
            SCREEN_ORIENTATION_SENSOR,
            SCREEN_ORIENTATION_NOSENSOR,
            SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            SCREEN_ORIENTATION_FULL_SENSOR,
            SCREEN_ORIENTATION_USER_LANDSCAPE,
            SCREEN_ORIENTATION_USER_PORTRAIT,
            SCREEN_ORIENTATION_FULL_USER,
            SCREEN_ORIENTATION_LOCKED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ScreenOrientation {
    }
}
