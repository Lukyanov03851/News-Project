package com.news.utils

class KeyHelper {

    init {
        System.loadLibrary("native-lib")
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     * @return News Api-Key
     */
    external fun newsApiKey(): String

}