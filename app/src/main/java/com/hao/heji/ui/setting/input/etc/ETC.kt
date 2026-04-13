package com.hao.heji.ui.setting.input.etc

/**
 *@date: 2023/2/12
 *@author: 锅得铁
 *#
 */
object ETC {
    const val URL =
        "http://hubeiweixin.u-road.com/HuBeiCityAPIServer/index.php/huibeicityserver/showetcsearch"
    const val ID = "42021909230571219224"
    const val CAR_ID = "鄂FNA518"

    //伪装User-Agent
    val USER_AGENTS =
        arrayOf( //"Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",
            "Mozilla/5.0 (Linux; Android 10; MI 8 Lite Build/QKQ1.190910.002; wv) ",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36"
        )
}