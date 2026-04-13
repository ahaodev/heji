package com.hao.heji.data.db

//----條件
const val YEAR = "%Y"
const val MONTH = "%Y-%m"
const val YEARMONTHDAY = "%Y-%m-%d"

fun dateFormat(year: String) = year
fun dateFormat(year: String, month: String) = "${year}-$month"
fun dateFormat(year: Int, month: Int, day: String) = "${year}-$month-$day"