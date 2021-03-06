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
 * @date 2017-01-17
 */
public enum ReplayStrategy {
    /** DO NOT USE: reserved. Equivalent to {@link ReplayStrategy#ENQUEUE_LAST_ONLY} */
    DEFAULT,
    /** Invocations are enqueued without discriminating */
    ENQUEUE,
    /** Only a single instance of a method call will be enqueued, if at all */
    ENQUEUE_LAST_ONLY,
    /**
     * Only the last invocation of a method call in a 'group' of related method calls will be
     * enqueued. This applies to methods that negate or interfere with each other. An example
     * would be showElement vs hideElement, which are antonyms.
     */
    ENQUEUE_LAST_IN_GROUP,
    /** Only a single instance of a method call with equivalent parameters will be enqueued, if at all */
    ENQUEUE_PARAM_UNIQUE,
    /** Invocations will not be enqueued */
    NONE
}