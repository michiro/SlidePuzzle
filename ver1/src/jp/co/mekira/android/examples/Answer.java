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

import java.util.Random;
import java.util.Vector;
import android.util.Log;

/**
 * パズルの答えを保持するクラス
 */
public class Answer {
    private int             numMoves;    //解答の手数
    private int[]           andPlaces;     //解答のX座標
    private int[]           ansDirections; //解答の移動方向
    private int             removedX;     //外した駒のX座標
    private int             removedY;     //外した駒のY座標
    private int             rows;
    private int             cols;
    private int             nextPlace;
    private Random          rand;
    private Integer[]       number;
    private Vector<Integer> candidate;

    public static final int DirectionError = 0;
    public static final int DirectionUp    = 1;
    public static final int DirectionDown  = 2;
    public static final int DirectionLeft  = 3;
    public static final int DirectionRight = 4;

    public Answer(int rows,int cols) {
        this.rows = rows;
        this.cols = cols;
        rand      = new Random(System.currentTimeMillis());

        number = new Integer[rows * cols];
        int n = 0;
        for (int y=0; y < rows; y++) {
            for (int x=0; x < cols; x++) {
                number[n] = new Integer(n);
                n++;
            }
        }

        candidate = new Vector<Integer>();
    }

    public int getNumMoves() {
        return numMoves;
    }
    public int getRemovedX() {
        return removedX;
    }
    public int getRemovedY() {
        return removedY;
    }
    public int getDirection(int n) {
        return ansDirections[n];
    }
    public int getPlace(int n) {
        return andPlaces[n];
    }

    /**
     * 次に動かす候補を選ぶ、前回動かした駒(prevPiece)は除外する
     * @param curX 現在空きになっているX座標
     * @param curY 現在空きになっているY座標
     */
    private void makeCandidate(int curX,int curY,int prevPlace) {
        candidate.removeAllElements();

        //上をチェック
        if (curY-1 >= 0 && prevPlace != getPNum(curX,curY-1)) {
            candidate.addElement(number[getPNum(curX,curY-1)]);
        }

        //下をチェック
        if (curY+1 <= cols-1 && prevPlace !=getPNum(curX,curY+1)) {
            candidate.addElement(number[getPNum(curX,curY+1)]);
        }

        //左をチェック
        if (curX-1 >= 0 && prevPlace != getPNum(curX-1,curY)) {
            candidate.addElement(number[getPNum(curX-1,curY)]);
        }

        //右をチェック
            if (curX+1 <= cols-1 && prevPlace != getPNum(curX+1,curY)) {
            candidate.addElement(number[getPNum(curX+1,curY)]);
        }
    }

    /**
     *
     */
    private int getDirection(int curX,int curY,int place) {
        int x = getXFromPlace(place);
        int y = getYFromPlace(place);

        if (curY < y) {
            return DirectionUp;
        } else if (curY > y) {
            return DirectionDown;
        }
        if (curX < x) {
            return DirectionLeft;
        } else if (curX > x) {
            return DirectionRight;
        }

        return DirectionError;
    }

    private int getPNum(int x,int y) {
        return (y*cols+x);
    }
    private int getXFromPlace(int place) {
        return (place % cols);
    }
    private int getYFromPlace(int place) {
        return (place / cols);
    }

    public void makeGame(int numMoves) {
        this.numMoves = numMoves;
        andPlaces     = new int[numMoves];
        ansDirections = new int[numMoves];

        //外す駒を決める
        removedX = Math.abs(rand.nextInt() % cols);
        removedY = Math.abs(rand.nextInt() % rows);
        nextPlace = getPNum(removedX,removedY);
        int prevPlace = -1;

        int curX = removedX;
        int curY = removedY;
        for (int i=0; i < numMoves; i++) {
            //次に動かす候補を集める
            makeCandidate(curX,curY,prevPlace);

            //候補選択
            int n            = Math.abs(rand.nextInt() % candidate.size());
            Integer place    = candidate.elementAt(n);
            andPlaces[i]     = place;
            ansDirections[i] = getDirection(curX,curY,place);

            curX = getXFromPlace(place);
            curY = getYFromPlace(place);

            prevPlace = nextPlace;
            nextPlace = place.intValue();
        }
    }

};
