package com.hao.heji.ui.create.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.hao.heji.R
import com.hao.heji.data.db.Image
import com.hao.heji.databinding.ItemPopSelectTicketImageBinding
import com.hao.heji.utils.ImageUtils

/**
 * 账单票据图片适配器
 * @date: 2020/9/16
 * @author: 锅得铁
 * #
 */
class BillPhotoAdapter :
    BaseQuickAdapter<Image, BaseViewHolder>(
        R.layout.item_pop_select_ticket_image,
        mutableListOf()
    ) {
    private lateinit var binding: ItemPopSelectTicketImageBinding
    override fun convert(holder: BaseViewHolder, image: Image) {
        binding = ItemPopSelectTicketImageBinding.bind(holder.itemView)
        val path = ImageUtils.getImagePath(image)
        Glide.with(binding.imgTicket)
            .asBitmap()
            .load(path)
            .error(R.drawable.ic_baseline_image_load_error_24)
            .placeholder(R.drawable.ic_baseline_image_24)
            .into(binding.imgTicket)
        //MyAppGlideModule.loadImageFile(App.context, image, binding.imgTicket)
        binding.imgDelete.visibility = View.VISIBLE
        binding.imgTicket.setOnClickListener { v ->
            showGallery(
                v as ImageView,
                holder.layoutPosition
            )
        }

    }

    init {
        addChildClickViewIds(R.id.imgDelete)
    }

    private fun showGallery(imageView: ImageView, startPosition: Int) {
        val srcViewUpdateListener =
            OnSrcViewUpdateListener { popupView: ImageViewerPopupView, position: Int ->
                popupView.updateSrcView(
                    recyclerView.getChildAt(startPosition).findViewById(R.id.imgTicket)
                )
            }

        XPopup.Builder(context)
            .asImageViewer(
                imageView,
                startPosition,
                ImageUtils.getPaths(data),
                false,
                false,
                -1,
                -1,
                -1,
                false,
                Color.rgb(32, 36, 46),
                srcViewUpdateListener,
                SmartGlideImageLoader(R.mipmap.ic_launcher),
                null
            )
            .show()
    }
}