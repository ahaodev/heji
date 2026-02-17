package com.hao.heji.ui.setting.input.etc.dto;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * @date: 2020/10/28
 * @author: 锅得铁
 * #
 */
public class ETCListInfoEntity {
    @Json(name ="status")
    public String status;
    @Json(name ="data")
    public List<Info> data;

    public static class Info {
       // @Json(name ="cardNo")
        public String cardNo;
        @Json(name ="payCardType")
        public String payCardType;
        @Json(name ="etcPrice")
        public int etcPrice;
        @Json(name ="exchargetime")
        public String exchargetime;
        @Json(name ="ex_enStationName")
        public String exEnStationName;
        @Json(name ="vehplate")
        public String vehplate;
        @Json(name ="province")
        public String province;
        @Json(name ="type")
        public int type;
    }
}
