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

import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;

public class SlidePuzzle1Activity extends Activity {
    private GameView gameView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("SlidePuzzle","onCreate");
        gameView = new GameView(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(gameView);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.d("SlidePuzzle","onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        gameView.onPause();
        //Log.d("SlidePuzzle","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        //Log.d("SlidePuzzle","onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d("SlidePuzzle","onResume");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //Log.d("SlidePuzzle","onRestart");
    }

    @Override
    public void onDestroy() {
        //Log.d("SlidePuzzle","onDestroy");
        if (gameView != null) {
            gameView.finish();
        }
        super.onDestroy();
    }

}
