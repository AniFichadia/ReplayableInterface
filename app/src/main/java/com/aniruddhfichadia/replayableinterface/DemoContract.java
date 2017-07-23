/*
 * Copyright (C) 2017 Aniruddh Fichadia
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If you use or enhance the code, please let me know using the provided author information or via email Ani.Fichadia@gmail.com.
 */
package com.aniruddhfichadia.replayableinterface;


/**
 * @author Aniruddh Fichadia
 * @date 2017-07-23
 */
public class DemoContract {
    @ReplayableInterface
    interface DemoUi {
        @ReplayableMethod(ReplayStrategy.NONE)
        void doMeaninglessThing();

        @ReplayableMethod(ReplayStrategy.ENQUEUE_PARAM_UNIQUE)
        void doSomethingElseMeaningLess(String aParam, boolean anotherParam);

        @ReplayableMethod(ReplayStrategy.ENQUEUE)
        void somethingEnqueueable(String aParam);

        @ReplayableMethod(value = ReplayStrategy.ENQUEUE_LAST_IN_GROUP, group = "loadingState")
        void showLoading();

        @ReplayableMethod(value = ReplayStrategy.ENQUEUE_LAST_IN_GROUP, group = "loadingState")
        void hideLoading();

        void setMessage(String text);

        void setLoadingAllowed(boolean allowed);


        int returnsSomething();
    }
}
