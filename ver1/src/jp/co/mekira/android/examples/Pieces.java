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

import android.util.Log;

/**
 * 全ての駒を保持するクラス
 */
public class Pieces {
    private int       rows;
    private int       cols;
    private Piece[][] pieces;
    private Piece     pTmp;

    public Pieces(int rows,int cols) {
        this.rows = rows;
        this.cols = cols;

        pieces = new Piece[rows][cols];
        for (int y=0; y < rows; y++) {
            for (int x=0; x < cols; x++) {
                pieces[y][x] = new Piece(y*cols+x);
            }
        }
        pTmp = new Piece(0);
    }

    public void reset() {
        for (int y=0; y < rows; y++) {
            for (int x=0; x < cols; x++) {
                pieces[y][x].reset();
            }
        }
    }

    public Piece getPiece(int x,int y) {
        return pieces[y][x];
    }
    public Piece getPiece(int place) {
        return pieces[place / cols][place % cols];
    }

    private int getX(int place) {
        return place % cols;
    }
    private int getY(int place) {
        return place / cols;
    }

    private int getPlace(int x,int y) {
        return (y * cols) + x;
    }
    private int getPlace(int place) {
        return getPlace(getX(place),getY(place));
    }

    private void swap(Piece p1,Piece p2) {
        pTmp.set(p1);
        p1.set(p2);
        p2.set(pTmp);
    }

    private void goUp(int place) {
        int x = getX(place);
        int y = getY(place);
        if (y > 0) {
            swap(pieces[y][x],pieces[y-1][x]);
        } else {
            Log.e("SlidePuzzle","goUp error y="+y);
        }
    }

    private void goDown(int place) {
        int x = getX(place);
        int y = getY(place);
        if (y < rows - 1) {
            swap(pieces[y][x],pieces[y+1][x]);
        } else {
            Log.e("SlidePuzzle","goDown error y="+y);
        }
    }

    private void goLeft(int place) {
        int x = getX(place);
        int y = getY(place);
        if (x > 0) {
            swap(pieces[y][x],pieces[y][x-1]);
        } else {
            Log.e("SlidePuzzle","goLeft error x="+x);
        }
    }

    private void goRight(int place) {
        int x = getX(place);
        int y = getY(place);
        if (x < cols - 1) {
            swap(pieces[y][x],pieces[y][x+1]);
        } else {
            Log.e("SlidePuzzle","goRight error x="+x);
        }
    }

    /**
     * 駒を移動させる
     */
    public void move(int place,int direction) {
        if (direction == Answer.DirectionUp) {
            goUp(place);
        } else if (direction == Answer.DirectionDown) {
            goDown(place);
        } else if (direction == Answer.DirectionLeft) {
            goLeft(place);
        } else if (direction == Answer.DirectionRight) {
            goRight(place);
        }
    }

    /**
     * 駒が移動可能かどうか調べる
     * @param place 調べる駒の位置
     * @return 移動できるなら移動方向、移動できない場合は0
     */
    public int isMovable(int place) {
        int x = getX(place);
        int y = getY(place);

        if (y > 0 && pieces[y-1][x].getRemoved()) {
            return Answer.DirectionUp;
        } else if (y < rows-1 && pieces[y+1][x].getRemoved()) {
            return Answer.DirectionDown;
        } else if (x > 0 && pieces[y][x-1].getRemoved()) {
            return Answer.DirectionLeft;
        } else if (x < cols-1 && pieces[y][x+1].getRemoved()) {
            return Answer.DirectionRight;
        }
        return 0;
    }

    /**
     *
     */
    public void setAnswer(Answer answer) {
        int numMoves = answer.getNumMoves();
        int startX = answer.getRemovedX();
        int startY = answer.getRemovedY();

        pieces[startY][startX].setRemoved(true);
        for (int i=0; i < numMoves; i++) {
            int place = answer.getPlace(i);
            int direction = answer.getDirection(i);
            int x = getX(place);
            int y = getY(place);

            if (direction == Answer.DirectionUp) {
                goUp(place);
            } else if (direction == Answer.DirectionDown) {
                goDown(place);
            } else if (direction == Answer.DirectionLeft) {
                goLeft(place);
            } else if (direction == Answer.DirectionRight) {
                goRight(place);
            } else {
                Log.e("SlidePuzzle","Direction error:"+direction);
            }
        }
    }

    public boolean checkComplete() {
        boolean complete = true;

        for (int y=0; y < rows; y++) {
            for (int x=0; x < cols; x++) {
                if (pieces[y][x].getOrgPlace() != pieces[y][x].getCurPlace()) {
                    complete = false;
                }
            }
        }
        return complete;
    }

    public void print() {
        Log.d("SlidePuzzle","print pieces ----------------------");
        for (int y=0; y < rows; y++) {
            String s = "";
            for (int x=0; x < cols; x++) {
                if (pieces[y][x].getRemoved()) {
                    s += " 00";
                } else if (pieces[y][x].getCurPlace() < 9) {
                    s += " 0" + (pieces[y][x].getCurPlace()+1);
                } else {
                    s += " " + (pieces[y][x].getCurPlace()+1);
                }
            }
            Log.d("SlidePuzzle",s);
        }
        Log.d("SlidePuzzle","----------------------");
    }
};
