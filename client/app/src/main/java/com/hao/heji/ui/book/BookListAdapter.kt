package com.hao.heji.ui.book

import android.view.View
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ImageUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.util.XPopupUtils
import com.hao.heji.config.Config
import com.hao.heji.R
import com.hao.heji.data.db.Book
import com.hao.heji.databinding.FragmentBookItemBinding
import com.hao.heji.utils.ColorUtils


/**
 * @date: 2021/7/9
 * @author: 锅得铁
 * #
 */
class BookListAdapter(val settingClickListener: (Book) -> Unit) :
    BaseQuickAdapter<Book, BaseViewHolder>(
        layoutResId = R.layout.fragment_book_item,
        mutableListOf()
    ) {

    private val colors = ColorUtils.groupColors()
    override fun convert(holder: BaseViewHolder, item: Book) {
        val binding = FragmentBookItemBinding.bind(holder.itemView)
        with(binding) {
            tvTitle.text = item.name
            tvContext.text = item.type

            val bannerBitmap = ImageUtils.drawable2Bitmap(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.banner_tree
                )
            )

            val isFirstChild = holder.layoutPosition == 0
            val backgroundDrawable = if (isFirstChild) {
                ImageUtils.bitmap2Drawable(ImageUtils.toRoundCorner(bannerBitmap, 10f))
            } else {
                val colorDrawable = colors[holder.layoutPosition % 24]
                XPopupUtils.createDrawable(colorDrawable, 10f, 10f, 10f, 10f)
            }
            root.background = backgroundDrawable

            imgSelected.visibility = if (item.id == Config.book.id) View.VISIBLE else View.INVISIBLE

            imgSetting.setOnClickListener {
                settingClickListener(item)
            }

            imgFirstBook.visibility = if (item.isInitial) View.VISIBLE else View.GONE
        }
    }
}