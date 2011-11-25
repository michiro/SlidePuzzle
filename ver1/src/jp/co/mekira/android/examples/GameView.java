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

import java.io.PrintWriter;
import java.io.StringWriter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;

/**
 * ゲーム本体のViewクラス
 */
public class GameView extends SurfaceView implements
                                          GestureDetector.OnGestureListener,
                                          SurfaceHolder.Callback,
                                          Runnable {

    private   GestureDetector gestureDetector;
    private   SurfaceHolder   holder;
    private   boolean         surfaceCreated;
    private   Thread          thread;
    private   int             status;
    private   int             prevStatus;
    private   boolean         loaded;
    private   DataLoader      loader;
    private   boolean         initializing;

    private   Paint           paint;
    private   Paint           bgPaint;
    private   Paint           textPaint;
    private   int             textSize;
    private   Bitmap          logoImg;

    private   int             xOff;         // 駒表示時のXのオフセット
    private   int             yOff;         // 駒表示時のYのオフセット
    private   int             pWidth;       // 駒の幅
    private   int             pHeight;      // 駒の高さ
    private   float           scale;        // 座標計算用スケール
    protected Bitmap[]        nImg;         // 数字が書いてある絵
    protected Bitmap[]        pImg;

    private   Answer          answer;
    private   Pieces          pieces;

    private   boolean         showMessage;
    private   int             movingLength;
    private   int             nDisplayAnswer;
    private   int             nAnswer;
    private   int             movingPlace;

    private   int             holdPiece;         //掴んでいる駒
    private   int             movableDirection;  //駒の移行中の向き
    private   int             pieceOffX; //駒移動時のX座標のオフセット
    private   int             pieceOffY; //駒移動時のY座標のオフセット
    private   int             moveDistanceX; //X方向の移動距離
    private   int             moveDistanceY; //X方向の移動距離
    private   int             velocityX;
    private   int             velocityY;
    private   int             bitmapAlpha;
    private   int             paintX;
    private   int             paintY;
    private   int             paintWidth;
    private   int             paintHeight;
    private   boolean         paintAll;

    private final int rows  = 4;
    private final int cols  = 4;
    private final int NumMoves = 10; // 解答の手数
    private final int numMoveToNext = 10; // この回数で次の駒まで移動

    private final int StatusNOP           = 0;
    private final int StatusInit          = 1;
    private final int StatusLoading       = 2;
    private final int StatusOpening       = 3;
    private final int StatusReady         = 4;
    private final int StatusStart         = 5;
    private final int StatusDisplayAnswer = 6;
    private final int StatusFling         = 7;
    private final int StatusPlaying       = 8;
    private final int StatusComplete      = 9;
    private final int StatusError         = 100;

    private final String[] msg = {
        "画面タップで開始",
        "ゲーム開始",
        "完成おめでとう",
    };

    public GameView(Activity activity) {
        super(activity);
        init();
    }

    /**
     * 初期化メソッド、コンストラクタから一度だけ呼ばれる
     */
    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        surfaceCreated = false;
        thread         = null;
        loader         = new DataLoader(this);
        loaded         = false;
        status         = StatusInit;
        initializing   = true;

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setARGB(0xff,0,0,0); //背景色

        paint = new Paint();
        paint.setARGB(0xff,0,0,0);

        textSize  = 32;
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setARGB(0xff,0xff,0xff,0xff);
        textPaint.setTextSize(textSize);

        Resources res = getContext().getResources(); 
        logoImg = BitmapFactory.decodeResource(res,R.drawable.fjtn);

        nImg   = new Bitmap[16];
        pImg   = new Bitmap[16];
        answer = new Answer(rows,cols);
        pieces = new Pieces(rows,cols);
        holdPiece = -1;

        thread = new Thread(this);
        thread.start();
    }

    /**
     * 初期化メソッド2、画像データがロードされた後に呼ばれる
     */
    private void init2() {
        //描画時の座標計算のための倍率、画面の横幅から決める
        if (getWidth() < 320) { // QVGA
            scale = 0.5F;
        } else if (getWidth() >= 320 && getWidth() < 480) { // HVGA
            scale = 0.67F;
        } else if (getWidth() >= 480 && getWidth() < 640) { // WVGA
            scale = 1.0F;
        } else if (getWidth() >= 640) {
            scale = (float)getWidth() / 480;
        }
        
        // X方向にセンタリングする為のオフセット
        xOff = (getWidth() - pImg[0].getWidth() * cols) / 2;
        
        // Y方向にセンタリングする為のオフセット
        yOff = (getHeight() - pImg[0].getHeight() * rows) / 2;

        pWidth  = pImg[0].getWidth();
        pHeight = pImg[0].getHeight();

        //駒は正方形である
        movingLength = pWidth / numMoveToNext;
    }

    /**
     * Bitmapを解放する
     */
    public void free() {
        if (logoImg != null) {
            logoImg.recycle();
            logoImg = null;
        }

        for (int i=0; i < pImg.length; i++) {
            if (pImg[i] != null) {
                pImg[i].recycle();
            }
            pImg[i] = null;
        }

        for (int i=0; i < nImg.length; i++) {
            if (nImg[i] != null) {
                nImg[i].recycle();
            }
            nImg[i] = null;
        }
    }

    public void onPause() {
        prevStatus = status;
    }

    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //Log.d("SlidePuzzle","SurfaceCreated");
        gestureDetector = new GestureDetector(this);
        surfaceCreated = true;

        if (initializing) {
            //Activity起動時
            repaint();
        }

        if (!loaded) {
            status = StatusLoading;
            wakeup();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceCreated  = false;
        gestureDetector = null;
        loaded = false;
        free();
    }
    
    /**
     * 1コマ分移動したか調べる。移動距離が多ければ減らす
     * @return trueなら移動している。falseなら移動しきっていない
     */
    private boolean checkMoveDistance() {
        boolean hit = true;

        if (movableDirection == Answer.DirectionUp) {
            if (moveDistanceY > 0) { //もとの位置から戻らないようにする
                moveDistanceY = 0;
            } else if (moveDistanceY < -pHeight) {
                moveDistanceY = -pHeight;
            } else {
                hit = false;
            }
        } else if (movableDirection == Answer.DirectionDown) {
            if (moveDistanceY < 0) {
                moveDistanceY = 0;
            } else if (moveDistanceY > pHeight) {
                moveDistanceY = pHeight;
            } else {
                hit = false;
            }
        } else if (movableDirection == Answer.DirectionLeft) {
            if (moveDistanceX > 0) {
                moveDistanceX = 0;
            } else if (moveDistanceX < -pWidth) {
                moveDistanceX = -pWidth;
            } else {
                hit = false;
            }
        } else if (movableDirection == Answer.DirectionRight) {
            if (moveDistanceX < 0) {
                moveDistanceX = 0;
            } else if (moveDistanceX > pWidth) {
                moveDistanceX = pWidth;
            } else {
                hit = false;
            }
        }

        return hit;
    }

    /**
     *
     */
    private void processOpening() {
        long t1,t2;
        int  delay;

        t1 = System.currentTimeMillis();
        repaint();
        bitmapAlpha -= 5;
        if (bitmapAlpha <= 0) {
            bitmapAlpha = 255;
            status = StatusReady;
            repaint();
        } else {
            t2 = System.currentTimeMillis();
            if (bitmapAlpha == 250) { //最初だけ少し待つ
                delay = 500 - (int)(t2 - t1);
            } else {
                delay = 20 - (int)(t2 - t1);
            }
            sleep(delay);
        }
    }

    /**
     *
     */
    private void processDisplayAnswer() {
        long t1,t2;
        int  delay;

        t1 = System.currentTimeMillis();
        repaint();
        nDisplayAnswer++;
        if (nDisplayAnswer > numMoveToNext) {
            nDisplayAnswer = 0;
            nAnswer++;
            pieces.move(movingPlace,movableDirection);
            if (nAnswer >= answer.getNumMoves()) {
                showMessage = true;
                status = StatusPlaying;
                repaint();
            } else {
                movingPlace      = answer.getPlace(nAnswer);
                movableDirection = answer.getDirection(nAnswer);
            }
        }
        t2 = System.currentTimeMillis();
        delay = 20 - (int)(t2 - t1);
        sleep(delay);
    }

    /**
     * フリックしたときに駒を飛ばす
     */
    private void processFling() {
        long t1,t2;
        int  delay;

        t1 = System.currentTimeMillis();
        if (getRepaintXY()) {
            repaint(paintX,paintY,paintWidth,paintHeight);
        }

        if (movableDirection == Answer.DirectionUp) {
            moveDistanceY += velocityY;
        } else if (movableDirection == Answer.DirectionDown) {
            moveDistanceY += velocityY;
        } else if (movableDirection == Answer.DirectionLeft) {
            moveDistanceX += velocityX;
        } else if (movableDirection == Answer.DirectionRight) {
            moveDistanceX += velocityX;
        }

        if (checkMoveDistance()) {
            //1コマ分移動した
            pieces.move(holdPiece,movableDirection);
            holdPiece = -1;
            if (pieces.checkComplete()) {
                pieces.reset();
                showMessage = true;
                status = StatusComplete;
            } else {
                status = StatusPlaying;
                repaint();
            }
        }

        t2 = System.currentTimeMillis();
        delay = 20 - (int)(t2 - t1);
        sleep(delay);
    }

    /**
     * ゲームスレッド
     */
    public void run() {
        while (thread != null) {
            if (status == StatusLoading) {
                sleep(100);
                if (loader.loadImages(getContext())) {
                    if (initializing) {
                        init2();
                        bitmapAlpha = 255;
                        status = StatusOpening;
                        initializing = false;
                    } else {
                        status = prevStatus;
                    }
                    loaded = true;
                    repaint();
                } else {
                    status = StatusError;
                }
            } else if (status == StatusOpening) {
                processOpening();
            } else if (status == StatusDisplayAnswer) {
                processDisplayAnswer();
            } else if (status == StatusComplete) {
                showMessage = showMessage ? false : true;
                repaint();
                sleep(500);
            } else if (status == StatusFling) {
                processFling();
            } else {
                synchronized (this) {
                    try {
                        wait();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
     * スレッドを止めてメモリを解放する。onDestroy()から呼ばれる。
     */
    public void finish() {
        thread = null;
        wakeup();
        free();
    }

    /**
     * 止めてあるスレッドを動かす
     */
    private void wakeup() {
        synchronized(this) {
            notifyAll();
        }
    }

    /**
     * スリープ
     * @param n スループする時間。単位はミリ秒。
     */
    private void sleep(long n) {
	if (n <= 0) {
	    return;
	}
	try {
	    Thread.sleep(n);
	} catch (Exception e) {
            Log.e("SlidePuzzle","sleep error:"+e.getMessage());
	}
    }

    /**
     * スタックトレースの文字列取得
     @ param throwable Throwableクラスのインスタンス
     * @return スタックトレースの文字列
     */
    public String getStackTrace(Throwable throwable) {
        String value = null;
        try {
            StringWriter sw = null;
            try {
                sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                value = sw.toString();
            } finally {
                if (sw != null) {
                    sw.close();
                }
            }
        } catch (Exception e) {
            value = throwable.getMessage();
        }
        return value;
    }
    
    ///////////////////////////////////////////////////////////////////////
    //
    //
    private void makeGame() {
        answer.makeGame(NumMoves);

        Piece p = pieces.getPiece(answer.getRemovedX(),answer.getRemovedY());
        p.setRemoved(true);
        //pieces.setAnswer(answer);

        nAnswer = 0;
        nDisplayAnswer = 0;
        movingPlace      = answer.getPlace(0);
        movableDirection = answer.getDirection(0);
    }

    private int getXFromPlace(int place) {
        return place % cols;
    }
    private int getYFromPlace(int place) {
        return place / cols;
    }

    /**
     * 駒の左上の座標とパッドをタッチした位置のオフセットを得る
     */
    private void getPieceOffsets(MotionEvent e,int place) {
        pieceOffX = (int)e.getX();
        pieceOffY = (int)e.getY();
    }

    /**
     * 駒を掴んでいるかどうか調べる
     * @param event モーションイベント
     * @return 掴んでいる駒番号。掴んでいないなら-1。
     */
    private int checkPieceHold(MotionEvent event) {
        int place = -1;
        int w = pWidth * cols;
        int h = pHeight * rows;

        moveDistanceX = moveDistanceY = 0;
        if (event.getX() >= xOff && event.getX() <= xOff+w &&
            event.getY() >= yOff && event.getY() <= yOff+h) {
            int x = (int)((event.getX() - xOff) / pWidth);
            int y = (int)((event.getY() - yOff) / pHeight);
            place = y * cols + x;

            Piece p = pieces.getPiece(place);
            if (p.getRemoved() ||
                (movableDirection = pieces.isMovable(place)) == 0) {
                //消された駒、移動できない駒は掴めない
                place = -1;
            }
        }

        return place;
    }
    
    /**
     * 駒を移動させる、実際の移動ではなくて表示位置の移動
     */
    private void setMoveDistances(MotionEvent event) {
        moveDistanceX = 0;
        moveDistanceY = 0;
        if (movableDirection == Answer.DirectionUp) {
            moveDistanceY = (int)(event.getY() - pieceOffY);
        } else if (movableDirection == Answer.DirectionDown) {
            moveDistanceY = (int)(event.getY() - pieceOffY);
        } else if (movableDirection == Answer.DirectionLeft) {
            moveDistanceX = (int)(event.getX() - pieceOffX);
        } else if (movableDirection == Answer.DirectionRight) {
            moveDistanceX = (int)(event.getX() - pieceOffX);
        }
        checkMoveDistance();
    }

    /**
     * 駒を移動させる。駒の大きさの半分以上移動している時に移動する。
     */
    private void movePiece(MotionEvent event) {
        setMoveDistances(event);        
        if (movableDirection == Answer.DirectionUp) {
            if (moveDistanceY > -pHeight / 2) {
                movableDirection = 0;
            }
        } else if (movableDirection == Answer.DirectionDown) {
            if (moveDistanceY < pHeight / 2) {
                movableDirection = 0;
            }
        } else if (movableDirection == Answer.DirectionLeft) {
            if (moveDistanceX > -pWidth / 2) {
                movableDirection = 0;
            }
        } else if (movableDirection == Answer.DirectionRight) {
            if (moveDistanceX < pWidth / 2) {
                movableDirection = 0;
            }
        }

        pieces.move(holdPiece,movableDirection);
    }

    /**
     * 移動中の描画エリアを出す
     */
    private boolean getRepaintXY() {
        int destination = -1;

        if (movableDirection == Answer.DirectionUp) {
            destination = holdPiece - 4;
            paintWidth  = pWidth;
            paintHeight = pHeight * 2;
        } else if (movableDirection == Answer.DirectionDown) {
            destination = holdPiece + 4;
            paintWidth  = pWidth;
            paintHeight = pHeight * 2;
        } else if (movableDirection == Answer.DirectionLeft) {
            destination = holdPiece - 1;
            paintWidth  = pWidth * 2;
            paintHeight = pHeight;
        } else if (movableDirection == Answer.DirectionRight) {
            destination = holdPiece + 1;
            paintWidth  = pWidth * 2;
            paintHeight = pHeight;
        }
        if (destination < 0) {
            return false;
        }

        int x1,y1,x2,y2;
        x1 = getXFromPlace(holdPiece) * pWidth + xOff;
        y1 = getYFromPlace(holdPiece) * pHeight + yOff;
        x2 = getXFromPlace(destination) * pWidth + xOff;
        y2 = getYFromPlace(destination) * pWidth + yOff;
        paintX = Math.min(x1,x2);
        paintY = Math.min(y1,y2);

        return true;
    }

    /**
     *
     */
    public boolean onDown(MotionEvent arg0) {
        //Log.d("SlidePuzzle","onDown");
        return false;
    }

    public void onLongPress(MotionEvent e) {
        //Log.d("SlidePuzzle","onLongPress");
    }

    public void onShowPress(MotionEvent e) {
        //Log.d("SlidePuzzle","onShowPress");
    }

    /**
     * スクロール時に呼ばれる
     */
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Log.d("SlidePuzzle","onScroll");
        return false;
    }

    /**
     * フリックされた時に呼ばれる
     */
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float velocityXX, float velocityYY) {

        //Log.d("SlidePuzzle","onFling");

        //移動できる方向と逆にフリックされたら無視
        if (movableDirection == Answer.DirectionUp && velocityYY > 0) {
            return false;
        } else if(movableDirection == Answer.DirectionDown && velocityYY < 0) {
            return false;
        } else if (movableDirection == Answer.DirectionLeft && velocityXX > 0) {
            return false;
        } else if (movableDirection == Answer.DirectionRight && velocityXX < 0) {
            return false;
        }

        velocityX = velocityY = 0;
        if (movableDirection == Answer.DirectionUp) {
            velocityY = -movingLength;
        } else if(movableDirection == Answer.DirectionDown) {
            velocityY = movingLength;
        } else if (movableDirection == Answer.DirectionLeft) {
            velocityX = -movingLength;
        } else if (movableDirection == Answer.DirectionRight) {
            velocityX = movingLength;
        }

        status = StatusFling;
        wakeup();

        return false;
    }

    /**
     * シングルタップ時に呼ばれる
     */
    public boolean onSingleTapUp(MotionEvent e) {
        boolean rc = false;

        //Log.d("SlidePuzzle","onSingleTapUp status="+status);
        if (status == StatusReady) {
            makeGame();
            status = StatusDisplayAnswer;
            wakeup();
            rc = true;
        }

        return rc;
    }

    /**
     * タッチパネルに触った時に呼ばれる
     */
    public boolean onTouchEvent(MotionEvent event) {
        boolean rc = false;

        //Log.d("SlidePuzzle","onTouchEvent status="+status);
        if (gestureDetector == null) {
            return false;
        }

        if (gestureDetector.onTouchEvent(event)) {
            rc = true;
        } else {
            rc = false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            return onActionUp(event);
        }

        if (status == StatusReady) {
            rc = true;
        } else if (status == StatusPlaying) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if ((holdPiece = checkPieceHold(event)) >= 0) {
                    getPieceOffsets(event,holdPiece);
                    if (showMessage) {
                        showMessage = false;
                    }
                    repaint();
                    paintAll = true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                setMoveDistances(event);
                if (paintAll) { //初回は全面書き換えをしておく
                    repaint();
                    paintAll = false;
                } else {
                    getRepaintXY();
                    repaint(paintX,paintY,paintWidth,paintHeight);
                }
            }
            rc = true;
        } else if (status == StatusComplete) {
            rc = true;
        }
        
        return rc;
    }

    private boolean onActionUp(MotionEvent event) {
        boolean rc = false;

        //Log.d("SlidePuzzle","onActionUp status="+status);
        if (status == StatusPlaying) {
            if (showMessage) {
                showMessage = false;
            }
            if (holdPiece >= 0) {
                movePiece(event);
                if (pieces.checkComplete()) {
                    pieces.reset();
                    showMessage = true;
                    status = StatusComplete;
                    wakeup();
                }
            }
            holdPiece = -1;
            repaint();
        } else if (status == StatusComplete) {
            status = StatusReady;
            repaint();
        }

        return rc;
    }

    /**
     * 全画面の描画
     */
    public void repaint() {
        Canvas canvas = null;

        if (!surfaceCreated) {
            return;
        }

        try {
            canvas = holder.lockCanvas();
            synchronized (holder) {
                onDraw(canvas);
            }
        } catch (Exception e) {
            Log.e("SlidePuzzle",""+getStackTrace(e));
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 画面の一部を描画
     * @param x 描画する左上のX座標
     * @param y 描画する左上のY座標
     * @param w 描画する幅
     * @param h 描画する高さ
     */
    public void repaint(int x,int y,int w,int h) {
        Canvas canvas = null;

        if (!surfaceCreated) {
            return;
        }

        try {
            canvas = holder.lockCanvas();
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            canvas.clipRect(x,y,x+w,y+h);

            synchronized (holder) {
                onDraw(canvas);
            }

            canvas.restore();
        } catch (Exception e) {
            Log.e("SlidePuzzle",""+getStackTrace(e));
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * ロゴの表示
     * @param canvas 描画時に使われるCanvas
     */
    private void paintLogo(Canvas canvas) {
        int xx,yy;
        Paint paint;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(0xff,0xff,0xff,0xff); //背景色
        canvas.drawRect(0,0,getWidth(),getHeight(),paint);
        if (logoImg != null) {
            xx = (getWidth() - logoImg.getWidth()) / 2;
            yy = (getHeight() - logoImg.getHeight()) / 2;
            canvas.drawBitmap(logoImg,xx,yy,paint);
        }
    }

    /**
     * 背景の描画
     * @param canvas 描画時に使われるCanvas
     */
    private void paintBg(Canvas canvas) {
        //背景を塗りつぶす
        canvas.drawRect(0,0,getWidth(),getHeight(),bgPaint);
    }

    /**
     * 駒の表示
     * @param canvas 描画時に使われるCanvas
     * @param drawNumber 数字の駒も描く
     */
    private void paintPieces(Canvas canvas,boolean drawNumber) {
        int n;
        
        n  = 0;
        for (int y=0; y < rows; y++) {
            for (int x=0; x < cols; x++) {
                int   xx = x * pWidth + xOff;
                int   yy = y * pHeight + yOff;
                if (n == movingPlace) {
                    if (movableDirection == Answer.DirectionUp) {
                        yy -= (movingLength * nDisplayAnswer);
                    } else if (movableDirection == Answer.DirectionDown) {
                        yy += (movingLength * nDisplayAnswer);
                    } else if (movableDirection == Answer.DirectionLeft) {
                        xx -= (movingLength * nDisplayAnswer);
                    } else if (movableDirection == Answer.DirectionRight) {
                        xx += (movingLength * nDisplayAnswer);
                    }
                }

                Piece p = pieces.getPiece(x,y);
                if (!p.getRemoved() && holdPiece != n) {
                    canvas.drawBitmap(pImg[p.getCurPlace()],xx,yy,bgPaint);
                    if (drawNumber) {
                        paint.setARGB(bitmapAlpha,0,0,0);
                        canvas.drawBitmap(nImg[p.getCurPlace()],xx,yy,paint);
                    }
                }
                n++;
            }
        }
    }

    /**
     * 移動させている駒の表示
     */
    private void paintHoldPiece(Canvas canvas,boolean drawNumber) {
        int x  = getXFromPlace(holdPiece);
        int y  = getYFromPlace(holdPiece);
        int xx = x * pWidth + xOff + moveDistanceX;
        int yy = y * pHeight + yOff + moveDistanceY;

        if (holdPiece < 0) {
            return;
        }

        //Log.d("SlidePuzzle","paintHoldPiece hold="+(holdPiece+1));
        Piece p = pieces.getPiece(x,y);
        canvas.drawBitmap(pImg[p.getCurPlace()],xx,yy,bgPaint);
        if (drawNumber) {
            canvas.drawBitmap(nImg[p.getCurPlace()],xx,yy,bgPaint);
        }

    }

    private void paintMessage(Canvas canvas) {
        int xx,yy;
        String str;

        if (status == StatusReady) {
            str = msg[0];
        } else if (status == StatusPlaying && showMessage) {
            str = msg[1];
        } else if (status == StatusComplete && showMessage) {
            str = msg[2];
        } else {
            return;
        }

        Rect   bounds = new Rect();
        textPaint.getTextBounds(str,0,str.length(),bounds);
        xx = (getWidth() - bounds.width()) / 2;
        yy = (getHeight() - textSize) / 2;
        canvas.drawText(str,xx,yy,textPaint);
    }

    /**
     * 描画
     * @param canvas 描画時に使われるCanvas
     */
    protected void onDraw(Canvas c) {
        //Log.d("SlidePuzzle","paint status="+status);
        if (status == StatusInit) {
            paintLogo(c);
        } else if (status == StatusOpening) {
            paintBg(c);
            paintPieces(c,true);
        } else if (status == StatusReady ||
                   status == StatusDisplayAnswer ||
                   status == StatusComplete) {
            paintBg(c);
            paintPieces(c,false);
            paintMessage(c);
        } else if (status == StatusPlaying ||
                   status == StatusFling) {
            boolean showNumber = true;
            paintBg(c);
            if (showMessage) {
                showNumber = false;
            }
            paintPieces(c,showNumber);
            paintHoldPiece(c,showNumber);
            paintMessage(c);
        }
    }
}
