package com.hao.heji.ui.calendar

import com.chad.library.adapter.base.entity.node.BaseNode
import com.haibin.calendarview.Calendar
import com.hao.heji.data.db.Image
import com.hao.heji.ui.base.IUiState
internal sealed interface CalenderUiState : IUiState {
    class DayBills(val data: Collection<BaseNode>) : CalenderUiState
    class Calender(val data: Map<String, Calendar>) : CalenderUiState
    class Images(val data: MutableList<Image>) : CalenderUiState
}
