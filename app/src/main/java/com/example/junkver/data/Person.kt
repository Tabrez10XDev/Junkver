package com.example.junkver.data

import android.net.Uri

data class Person(
    var uid : String ?= null,
    var username : String ?= null,
    var uri : Uri ?= null
)
