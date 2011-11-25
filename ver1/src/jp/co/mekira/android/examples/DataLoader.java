/**
 * Copyright (C) 2011 Mekira Net Systems Co,.Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.mekira.android.examples.slidepuzzle1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * データをロードするためのクラス
 */
public class DataLoader {
    private GameView game;

    private final int[] imageIds = {
        R.drawable.p01,
        R.drawable.p02,
        R.drawable.p03,
        R.drawable.p04,
        R.drawable.p05,
        R.drawable.p06,
        R.drawable.p07,
        R.drawable.p08,
        R.drawable.p09,
        R.drawable.p10,
        R.drawable.p11,
        R.drawable.p12,
        R.drawable.p13,
        R.drawable.p14,
        R.drawable.p15,
        R.drawable.p16,

        R.drawable.n01,
        R.drawable.n02,
        R.drawable.n03,
        R.drawable.n04,
        R.drawable.n05,
        R.drawable.n06,
        R.drawable.n07,
        R.drawable.n08,
        R.drawable.n09,
        R.drawable.n10,
        R.drawable.n11,
        R.drawable.n12,
        R.drawable.n13,
        R.drawable.n14,
        R.drawable.n15,
        R.drawable.n16,
    };

    /**
     * DataLoaderクラスのコンストラクタ
     * @param game GameViewクラスのインスタンス
     */
    public DataLoader(GameView game) {
        this.game = game;
    }

    /**
     * 画像データをロードする
     * @param context コンテキスト
     * @return true 正常にロードできた場合。 false 失敗した場合
     */
    public boolean loadImages(Context context) {
        Resources res = context.getResources(); 

        int n = 0;
        for (int i=0; i < 16; i++) {
            Bitmap b;
            if ((game.pImg[i] =
                 BitmapFactory.decodeResource(res,imageIds[n])) == null) {
                Log.e("SlidePuzzle","loadImages err:"+imageIds[n]);
                return false;
            }
            n++;
        }

        //数字が書いてある絵
        for (int i=0; i < 16; i++) {
            Bitmap b;
            if ((game.nImg[i] =
                 BitmapFactory.decodeResource(res,imageIds[n])) == null) {
                Log.e("SlidePuzzle","loadImages err:"+imageIds[n]);
                return false;
            }
            n++;
        }

        return true;
    }
}

