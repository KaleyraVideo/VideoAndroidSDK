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

package com.kaleyra.video_glasses_sdk.menu

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kaleyra.video.conference.Effect.Video.None
import com.kaleyra.video.conference.Effect.Video.None.id
import com.kaleyra.video_glasses_sdk.R
import com.kaleyra.video_glasses_sdk.call.CallAction
import com.kaleyra.video_glasses_sdk.utils.extensions.ContextExtensions.getAttributeResourceId
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassMenuItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A menu item. If the active text is null, isActivated will always be false.
 *
 * @property action The action call action
 * @constructor
 */
internal class MenuItem(val action: CallAction) : AbstractItem<MenuItem.ViewHolder>() {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = action.hashCode().toLong()

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = action.hashCode()

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = action.layoutRes

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun createView(ctx: Context, parent: ViewGroup?): View {
        val layout = LayoutInflater.from(
            ContextThemeWrapper(ctx, ctx.theme.getAttributeResourceId(action.styleAttr))
        ).inflate(layoutRes, parent, false).apply { id = action.viewId }

        if (action is CallAction.CUSTOM) {
            layout.findViewById<TextView>(R.id.kaleyra_text)?.let { actionText ->
                actionText.setText(action.text)
            }
        }

        return layout
    }


    /**
     * View holder for menu item
     *
     * @constructor
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<MenuItem>(view) {

        private val binding = KaleyraGlassMenuItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: MenuItem, payloads: List<Any>) {
            item.action.binding = binding
            item.action.onReady()
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: MenuItem) {
            item.action.binding = null
        }
    }
}