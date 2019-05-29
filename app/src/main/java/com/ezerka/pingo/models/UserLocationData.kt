package com.ezerka.pingo.models

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class UserLocationData(
    var geoPoint: GeoPoint? = null, @ServerTimestamp var timestamp: Date? = null,
    var user: UserData? = null
)