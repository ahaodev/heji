package com.hao.heji.data

/**
 * 软删除和同步状态常量
 */
object Status {
    /** 未删除 */
    const val NOT_DELETED = 0
    /** 已删除（软删除） */
    const val DELETED = 1

    /** 未同步 */
    const val NOT_SYNCED = 0
    /** 已同步 */
    const val SYNCED = 1
}
