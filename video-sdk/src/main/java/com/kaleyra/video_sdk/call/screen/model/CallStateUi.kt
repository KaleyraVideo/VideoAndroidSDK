/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.screen.model

/**
 * Representation of the Call State on the Ui
 */
sealed class CallStateUi {

    /**
     * Ringing Call State Ui
     */
    data object Ringing : CallStateUi()

    /**
     * Ringing Remotely Call State Ui
     */
    data object RingingRemotely : CallStateUi()

    /**
     * Dialing Call State Ui
     */
    data object Dialing : CallStateUi()

    /**
     * Connecting Call State Ui
     */
    data object Connecting: CallStateUi()

    /**
     * Connected Call State Ui
     */
    data object Connected : CallStateUi()

    /**
     * Reconnecting Call State Ui
     */
    data object Reconnecting : CallStateUi()

    /**
     * Disconnecting Call State Ui
     */
    data object Disconnecting : CallStateUi()

    /**
     * Disconnected Call State Ui
     */
    sealed class Disconnected : CallStateUi() {

        /**
         * Disconnected Call State Ui
         */
        companion object : Disconnected()

        /**
         * Ended Call State Ui Instance
         */
        sealed class Ended : Disconnected() {

            /**
             * Call State Ui Instance
             */
            companion object : Ended()

            /**
             * HungUp Call State Ui
             */
            data object HungUp: Ended()

            /**
             * Declined Call State Ui
             */
            data object Declined : Ended()

            /**
             * Kicked Call State Ui
             * @property adminName String participant's identifier that kicked out the logged user
             * @constructor
             */
            data class Kicked(val adminName: String) : Ended()

            /**
             * Answered On Another Device
             */
            data object AnsweredOnAnotherDevice : Ended()

            /**
             * Line Busy Call State Ui
             */
            data object LineBusy : Ended()

            /**
             * Current User In Another Call Call State Ui
             */
            data object CurrentUserInAnotherCall: Ended()

            /**
             * Timeout Call State Ui
             */
            data object Timeout : Ended()

            /**
             * Error Call State Ui
             */
            sealed class Error: Ended() {

                /**
                 * Error Call State Ui Instance
                 */
                companion object : Error()

                /**
                 * Server Error Call State Ui
                 */
                data object Server : Error()

                /**
                 * Unknown Error Call State Ui
                 */
                data object Unknown : Error()
            }
        }
    }
}
