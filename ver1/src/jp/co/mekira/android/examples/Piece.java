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

/**
 * 駒の位置を保持するクラス
 */
public class Piece {
    private int     orgPlace;  // 完成時の位置
    private int     curPlace;  // 現在のY位置
    private boolean removed;   // 外された駒ならtrue

    public Piece(int place) {
        orgPlace  = place;
        curPlace  = place;
        removed   = false;
    }

    public boolean isEqual(Piece p) {
        if (this.orgPlace == p.getOrgPlace()) {
            return true;
        }
        return false;
    }

    public void set(Piece p) {
        curPlace = p.getCurPlace();
        removed  = p.getRemoved();
    }

    public void reset() {
        curPlace = orgPlace;
        removed  = false;
    }

    public int getOrgPlace() {
        return orgPlace;
    }

    public int getCurPlace() {
        return curPlace;
    }

    public void setCurPlace(int p) {
        curPlace = p;
    }

    public boolean getRemoved() {
        return removed;
    }

    public void setRemoved(boolean r) {
        removed = r;
    }
};
