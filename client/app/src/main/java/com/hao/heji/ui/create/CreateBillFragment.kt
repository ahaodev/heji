package com.hao.heji.ui.create

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lxj.xpopup.XPopup
import com.hao.heji.*
import com.hao.heji.config.Config
import com.hao.heji.data.Status
import com.hao.heji.data.BillType
import com.hao.heji.data.converters.DateConverters
import com.hao.heji.data.converters.MoneyConverters.ZERO_00
import com.hao.heji.data.db.*
import com.hao.heji.databinding.FragmentCreatebillBinding
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.base.FragmentViewPagerAdapter
import com.hao.heji.ui.base.render
import com.hao.heji.ui.category.manager.CategoryManagerFragmentArgs
import com.hao.heji.ui.create.*
import com.hao.heji.ui.popup.SelectImagePopup
import com.hao.heji.utils.YearMonth
import com.hao.heji.widget.KeyBoardView.OnKeyboardListener
import java.math.BigDecimal
import java.util.*

/**
 * 添加账单（支出/收入）
 * -----------title------------
 * 收入|
 * -----------category---------
 * 账单类别
 * ----------
 */
class CreateBillFragment : BaseFragment() {

    internal val viewModel by koinViewModel<CreateBillViewModel>()

    val binding: FragmentCreatebillBinding by lazy {
        FragmentCreatebillBinding.inflate(layoutInflater)
    }
    private val pagerAdapter: FragmentViewPagerAdapter by lazy {
        FragmentViewPagerAdapter(
            childFragmentManager,
            lifecycle,
            listOf(
                CategoryFragment.newInstance(BillType.EXPENDITURE),
                CategoryFragment.newInstance(BillType.INCOME)
            ),
            listOf(
                BillType.EXPENDITURE.label, BillType.INCOME.label
            )
        )
    }

    // 使用 Android PhotoPicker API 选择图片
    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(SelectImagePopup.SELECT_MAX_COUNT)
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@registerForActivityResult
        LogUtils.d("PhotoPicker selected ${uris.size} items")

        val mSelected = uris.map { uri ->
            UriUtils.uri2File(uri).absolutePath
        }

        val popup = _popupSelectImage ?: return@registerForActivityResult
        val existingImages = popup.getImages()
        val existingPaths = existingImages.map { it.localPath }.toSet()
        // 去重后追加新选择的图片
        val newImages = mSelected.filter { it !in existingPaths }.map { selectPath ->
            Image(billID = mBill.id).apply {
                localPath = selectPath
            }
        }
        val allImages = (existingImages + newImages).toMutableList()
        popup.setImages(allImages)
    }

    private var _popupSelectImage: SelectImagePopup? = null

//图片弹窗
    val popupSelectImage by lazy {
        SelectImagePopup(requireActivity()).apply {
            deleteListener = {
                viewModel.deleteImage(it.id)
            }
            selectedImagesCall = {
                getImagesPath()
            }
            selectListener = { _ ->
                pickMultipleMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }.also { _popupSelectImage = it }
    }

    /**
     * 是否是修改账单
     * 当isModify为true时为要修改的账单
     * 默认新增
     */
    private var isModify = false

    /**
     * 使用 bill控制页面（TODO 优化为vm托管bill，页面回复后根据vm bill重建）
     */
    private lateinit var mBill: Bill

    override fun layout() = binding.root

    /**
     * 修改时预先选中类别
     *
     * @param category
     * @param type
     */
    private fun setSelectCategory(category: String, type: Int) {
        //内容页绘制完成后选中类别
        binding.vpContent.post {
            val index = if (type == BillType.EXPENDITURE.value) 0 else 1
            val categoryFragment = pagerAdapter.getFragment(index) as CategoryFragment
            categoryFragment.setSelectCategory(category)
        }
    }

    /**
     *
     * @see CategoryFragment.setCategories
     * @param type
     * @param categories
     */
    private fun setCategories(type: Int, categories: MutableList<Category>) {
        LogUtils.d(
            "TimeTest",
            TimeUtils.millis2String(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss")
        )
        val index = if (type == BillType.EXPENDITURE.value) 0 else 1
        val categoryFragment = pagerAdapter.getFragment(index) as CategoryFragment
        categoryFragment.setCategories(categories)
        val billType = BillType.fromValue(type)
        binding.keyboard.setType(billType)
        val color = if (billType == BillType.EXPENDITURE) R.color.expenditure else R.color.income
        binding.tvMoney.setTextColor(resources.getColor(color, null))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        /**
         * 选择照片
         */
        val mArgs = CreateBillFragmentArgs.fromBundle(requireArguments()).argAddBill
        isModify = mArgs.isModify
        mBill = mArgs.bill ?: Bill(time = Date(), bookId = Config.book.id)
        LogUtils.d(mBill.toString())
    }

    private fun showPager() {
        binding.vpContent.adapter = pagerAdapter
        TabLayoutMediator(binding.tab, binding.vpContent) { tab, position ->
            tab.text = pagerAdapter.textList[position]
        }.attach()
        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                LogUtils.d("onTabSelected", tab.position)
                val type =
                    if (tab.position == 0) BillType.EXPENDITURE.value else BillType.INCOME.value
                viewModel.getCategories(type)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                LogUtils.d("onTabUnselected", tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                LogUtils.d("onTabReselected", tab.position)
            }
        })
    }

    override fun initView(rootView: View) {
        binding.imgAddCategory.setOnClickListener {
            findNavController().navigate(
                R.id.nav_category_manager,
                CategoryManagerFragmentArgs(mBill.type).toBundle()
            )
        }
        showPager()
        binding.imgTicket.setOnClickListener {
            XPopup.Builder(requireContext())
                .asCustom(popupSelectImage)
                .show()
        }
        keyboardListener()
        with(mBill) {
            setTime(time)
            category?.let { setSelectCategory(it, type) }
            setMoney(money)
            images.let {
                viewModel.getImages(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(viewModel) { uiState ->
            when (uiState) {
                is CreateBillUIState.BillChange -> {
                    val bill = uiState.bill
                    LogUtils.d(bill)
                }

                is CreateBillUIState.Images -> {
                    val images = uiState.images
                    LogUtils.d(uiState.images)
                    popupSelectImage.setImage(images)
                }

                is CreateBillUIState.Error -> {
                    ToastUtils.showLong(uiState.throws.message)
                }

                is CreateBillUIState.Finish -> {
                    findNavController().popBackStack()
                }

                is CreateBillUIState.SaveAgain -> {
                    mBill = Bill()
                    binding.keyboard.clear()
                    binding.eidtRemark.setText("")
                    binding.tvMoney.text = "0"
                    popupSelectImage.clear()
                }

                is CreateBillUIState.Categories -> {
                    setCategories(uiState.type, uiState.categories)
                }

                is CreateBillUIState.SubCategories -> {
                    val index = if (uiState.type == BillType.EXPENDITURE.value) 0 else 1
                    val categoryFragment = pagerAdapter.getFragment(index) as CategoryFragment
                    categoryFragment.setSubCategories(uiState.children)
                }
            }
        }
    }

    override fun setUpToolBar() {
        super.setUpToolBar()
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * 选择小时和分钟
     *
     * @param yearTime 年份-月份
     */
    private fun selectHourAndMinute(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hourOfDay: Int,
        minute: Int
    ) {
        val onTimeSetListener =
            OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                if (hourOfDay == 0 && minute == 0) return@OnTimeSetListener
                val selectTime =
                    "$year-$month-$dayOfMonth $hourOfDay:$minute:00"//yyyy-MM-dd hh:mm:00
                setTime(DateConverters.str2Date(selectTime))
            }
        val timePickerDialog =
            TimePickerDialog(mainActivity, onTimeSetListener, hourOfDay, minute, true)
        timePickerDialog.show()
    }


    private fun keyboardListener() {
        binding.keyboard.keyboardListener = object : OnKeyboardListener {
            override fun save(result: String) {
                ToastUtils.showLong(result)
                mBill.images = popupSelectImage.getImagesPath()
                this@CreateBillFragment.save(false)
            }

            override fun calculation(result: String) {
                binding.tvMoney.text = result
            }

            override fun saveAgain(result: String) {
                ToastUtils.showLong(result)
                this@CreateBillFragment.save(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val stack: Stack<String>? = viewModel.keyBoardStack
        if (!stack.isNullOrEmpty()) {
            binding.keyboard.post {
                binding.keyboard.stack = stack
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.keyBoardStack = binding.keyboard.stack
    }

    /**
     * @param type 首先返回Type保证页面响应
     * @param category
     */
    fun selectedCategory(type: Int, category: Category?) {
        LogUtils.d("selectedCategory : type=$type category= $category")
        val billType = BillType.fromValue(type)
        mBill.type = billType.value
        if (null != category) {
            mBill.category = category.name
        } else {
            mBill.category = null
        }
    }

    private fun setMoney(money: BigDecimal) {
        //填充money到键盘，抹0再输入到键盘
        with(money.toPlainString()) {
            if (contains(".00"))
                replace(".00", "")
            else
                this
        }.forEach { element ->
            binding.keyboard.input(element.toString())
        }
        //填充输入信息
        binding.apply {
            tvMoney.text = mBill.money.toString()
            tvBillTime.text = mBill.time.string()
            mBill.remark?.let { remark ->
                eidtRemark.setText(remark)
            }
            val existImages = mBill.images.isNotEmpty()
            if (existImages) {
                imgTicket.text = "图片(x${mBill.images.size})"
            }
        }
    }


    private fun setTime(selectTime: Date) {
        binding.tvBillTime.text = DateConverters.date2Str(selectTime)
        mBill.time = selectTime
        LogUtils.d(selectTime)
        binding.tvBillTime.text = mBill.time.string() //设置日历初始选中时间
        binding.tvBillTime.setOnClickListener {
            val onDateSetListener =
                OnDateSetListener { datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    val selectCalendar = mBill.time.calendar()
                    selectCalendar[year, month] = dayOfMonth
                    binding.tvBillTime.text = selectCalendar.time.string()
                    mBill.time = selectTime
                    selectHourAndMinute(
                        year = year,
                        month = month + 1,//实际保存时，选择的时间需要+1（month：0-11 ）
                        dayOfMonth = dayOfMonth,
                        hourOfDay = selectCalendar[Calendar.HOUR_OF_DAY],
                        minute = selectCalendar[Calendar.MINUTE]
                    )
                }
            val yearMonth = YearMonth.format(mBill.time)
            val dialog = DatePickerDialog(
                mainActivity,
                onDateSetListener,
                yearMonth.year,
                yearMonth.month - 1,
                yearMonth.day
            )
            dialog.setOnDateSetListener(onDateSetListener)
            dialog.show()
        }
    }


    private fun save(again: Boolean) {
        try {
            if (isModify) {
                if (mBill.crtUser != Config.user.id) {
                    ToastUtils.showLong("非本人创建的账单，无权修改")
                    return
                }
                mBill.synced = Status.NOT_SYNCED
            }
            mBill.bookId = Config.book.id
            mBill.remark = binding.eidtRemark.text.toString()
            mBill.crtUser = Config.user.id
//            mBill.type = pagerAdapter.getItem()
            mBill.money = BigDecimal(binding.tvMoney.text.toString())
            //check value is false throw error
            check(mBill.bookId != "") { "账本ID异常" }
            check(mBill.money != ZERO_00()) { "金额不能为 ${ZERO_00().toPlainString()}" }
            check(mBill.money != BigDecimal.ZERO) { "金额不能为 ${BigDecimal.ZERO.toPlainString()}" }
            check(mBill.category != null) { "未选类别" }
            LogUtils.d(mBill)
            viewModel.save(mBill, again)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showLong(e.message)
        }
    }

}