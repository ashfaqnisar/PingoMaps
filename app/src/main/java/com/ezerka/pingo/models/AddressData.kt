package com.ezerka.pingo.models

import com.google.maps.model.Distance
import com.google.maps.model.Duration
import com.google.android.gms.maps.model.LatLng

data class AddressData(
    var pickupAddress: String? = "",
    var destAddress: String? ="",
    var pickuplatlng: LatLng? = null,
    var destLatLng: LatLng? =null,
    var distance: Distance? = null,
    var duration: Duration?= null
)
