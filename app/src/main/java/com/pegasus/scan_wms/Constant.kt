package com.pegasus.scan_wms

object Constant {
    //urlpublica http://138.219.12.234:8084/WMS_WebService_Android.asmx/
    //urllocal http://192.168.10.45:8084/WMS_WebService_Android.asmx/

    const val URL = "http://138.219.12.234:8084/WMS_WebService_Android.asmx/"
}

data class Item (val ItemId : String, val ItemNombre : String )