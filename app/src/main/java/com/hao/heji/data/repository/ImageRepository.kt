package com.hao.heji.data.repository

import com.hao.heji.App

/**
 * @date: 2022/4/2
 * @author: 锅得铁
 * #
 */
class ImageRepository {
    private val imgDao = App.dataBase.imageDao()

    fun preDelete(imageID: String) {
        imgDao.preDelete(imageID = imageID)
    }

    fun findImages(ids: List<String>) = imgDao.findImage(ids)

    fun findImagesIdByBillIds(billIds: List<String>) = imgDao.findImagesIdByBillIds(billIds)

    fun findByBillId(billId: String) = imgDao.findByBillId(billId)
}
