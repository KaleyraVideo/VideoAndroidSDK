package com.kaleyra.video_common_ui.utils

import android.os.Bundle
import org.json.JSONObject

internal fun Bundle.toJSONObject(): JSONObject {
    val json = JSONObject()
    val keys = keySet()
    for (key in keys) {
        runCatching { json.put(key, get(key)) }.onFailure { it.printStackTrace() }
    }
    return json
}