package com.hao.heji.ui.popup

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.App
import com.hao.heji.BuildConfig
import com.hao.heji.R
import com.hao.heji.config.Config
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Image
import com.hao.heji.databinding.ItemImgBinding
import com.hao.heji.databinding.PopLayoutBilliInfoBinding
import com.hao.heji.string
import java.util.Date
import com.hao.heji.ui.MainActivity
import com.hao.heji.ui.create.ArgAddBill
import com.hao.heji.ui.create.CreateBillFragmentArgs
import com.hao.heji.utils.ImageUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.lxj.xpopup.util.XPopupUtils

/**
 * @date: 2020/9/20
 * @author: 锅得铁
 * #
 */
class PopupBillInfo(
    val activity: MainActivity,
    val delete: (Bill) -> Unit, val update: (Bill) -> Unit
) : BottomPopupView(activity) {
    private lateinit var mBill: Bill

    companion object {
        fun create(
            activity: MainActivity,
            delete: (Bill) -> Unit,
            update: (Bill) -> Unit,
        ): PopupBillInfo {
            return XPopup.Builder(activity).hasNavigationBar(false)
                .asCustom(PopupBillInfo(activity, delete, update)) as PopupBillInfo
        }
    }

    fun show(bill: Bill) {
        mBill = bill
        show()
        post {
            binding.apply {
                tvMonney.text = mBill.money.toString()
                tvType.text = mBill.category
                tvRecordTime.text = Date(mBill.crtTime).string()
                tvTicketTime.text = mBill.time.string()
                rePeople.text = mBill.crtUser
                if (!mBill.remark.isNullOrBlank()) {
                    textViewRemarkLabel.visibility = View.VISIBLE
                    tvRemark.visibility = View.VISIBLE
                    tvRemark.text = mBill.remark
                } else {
                    textViewRemarkLabel.visibility = View.GONE
                    tvRemark.visibility = View.GONE
                }
            }
        }
    }

    lateinit var binding: PopLayoutBilliInfoBinding
    private var imageAdapter = ImageAdapter()

    override fun getImplLayoutId(): Int {
        return R.layout.pop_layout_billi_info
    }

    override fun onCreate() {
        super.onCreate()
        binding = PopLayoutBilliInfoBinding.bind(popupContentView.findViewById(R.id.billInfoCard))
        binding.tvDelete.setOnClickListener {
            deleteTip()
        }
        binding.tvUpdate.setOnClickListener {
            update(mBill)
            val bundle =
                CreateBillFragmentArgs(ArgAddBill(isModify = true, mBill)).toBundle()
            activity.navController.navigate(R.id.nav_bill_add, bundle)
            dismiss()
        }
        //设置圆角背景
        popupImplView.background = XPopupUtils.createDrawable(
            resources.getColor(R.color._xpopup_light_color, null),
            popupInfo.borderRadius, popupInfo.borderRadius, 0f, 0f
        )

        initBillImageList()//初始化列表和适配器
    }

    private fun initBillImageList() {
        binding.ticketRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }


    /**
     * 删除该条账单
     */
    private fun deleteTip() {
        XPopup.Builder(context).asConfirm(
            "删除提示", "确认删除该条账单吗？"
        ) {
            context as MainActivity
            if (mBill.crtUser != Config.user.id) {
                ToastUtils.showLong("只有账单创建人有权删除该账单")
                return@asConfirm
            }
            //状态删除
            App.dataBase.billDao().preDelete(mBill.id,Config.user.id)
            delete(mBill)
            dismiss()
        }.show()
    }

    fun setImages(data: List<Image>) {
        if (data.isEmpty()) return
        //服务器返回的是图片的ID、需要加上前缀
        val imagePaths = data.map { image: Image ->
            val onlinePath = image.onlinePath
            if (onlinePath != null && !image.onlinePath!!.contains("http")) { //在线Image路径
                val path = BuildConfig.HTTP_URL + "/image/" + image.onlinePath
                image.onlinePath = path
            }
            image
        }.toMutableList()
        imageAdapter.setNewInstance(imagePaths)
    }

    /**
     * 票据图片Adapter
     */
    private class ImageAdapter : BaseQuickAdapter<Image, BaseViewHolder>(R.layout.item_img) {
        lateinit var binding: ItemImgBinding
        override fun convert(holder: BaseViewHolder, item: Image) {
            binding = ItemImgBinding.bind(holder.itemView)
            val path = ImageUtils.getImagePath(item)
            Glide.with(binding.itemImage)
                .asBitmap()
                .load(path)
                .error(R.drawable.ic_baseline_image_load_error_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(binding.itemImage)
            binding.itemImage.setOnClickListener { v: View? -> showGallery(holder.layoutPosition) }
            LogUtils.d(
                "local path:${item.localPath}",
                "online path:${item.onlinePath}",
                "use path:${path}"
            )
        }

        private fun showGallery(startPosition: Int) {
            val srcViewUpdateListener =
                OnSrcViewUpdateListener { popupView: ImageViewerPopupView, position: Int ->
                    popupView.updateSrcView(recyclerView.getChildAt(position) as ImageView)
                }
            XPopup.Builder(context)
                .asImageViewer(
                    recyclerView.getChildAt(startPosition) as ImageView,
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
}

