package com.hao.heji.ui.create

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
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

    private var _popupSelectImage: SelectImagePopup? = null

    val popupSelectImage by lazy {
        SelectImagePopup(requireActivity()).apply {
            deleteListener = { viewModel.deleteImage(it.id) }
            selectedImagesCall = { getImagesPath() }
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

    private lateinit var mBill: Bill

    override fun layout() = binding.root

    // region Lifecycle

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val mArgs = CreateBillFragmentArgs.fromBundle(requireArguments()).argAddBill
        isModify = mArgs.isModify
        mBill = mArgs.bill ?: Bill(time = Date(), bookId = Config.book.id)
        LogUtils.d(mBill.toString())
    }

    override fun initView(rootView: View) {
        setupToolbarAction()
        setupPager()
        setupImagePicker()
        setupKeyboard()
        if (isModify) {
            restoreBillFields()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(viewModel) { uiState ->
            when (uiState) {
                is CreateBillUIState.BillChange -> LogUtils.d(uiState.bill)

                is CreateBillUIState.Images -> {
                    LogUtils.d(uiState.images)
                    popupSelectImage.setImage(uiState.images)
                }

                is CreateBillUIState.Error -> ToastUtils.showLong(uiState.throws.message)

                is CreateBillUIState.Finish -> findNavController().popBackStack()

                is CreateBillUIState.SaveAgain -> resetForNewBill()

                is CreateBillUIState.Categories -> {
                    applyCategories(uiState.type, uiState.categories)
                }

                is CreateBillUIState.SubCategories -> {
                    val index = if (uiState.type == BillType.EXPENDITURE.value) 0 else 1
                    val categoryFragment = pagerAdapter.getFragment(index) as CategoryFragment
                    categoryFragment.setSubCategories(uiState.children)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val stack: Stack<String>? = viewModel.keyBoardStack
        if (!stack.isNullOrEmpty()) {
            binding.keyboard.post { binding.keyboard.stack = stack }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.keyBoardStack = binding.keyboard.stack
    }

    override fun setUpToolBar() {
        super.setUpToolBar()
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    // endregion

    // region Setup

    private fun setupToolbarAction() {
        binding.imgAddCategory.setOnClickListener {
            findNavController().navigate(
                R.id.nav_category_manager,
                CategoryManagerFragmentArgs(mBill.type).toBundle()
            )
        }
    }

    private fun setupPager() {
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

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // 使用 Android PhotoPicker API 选择图片
    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(SelectImagePopup.SELECT_MAX_COUNT)
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@registerForActivityResult
        LogUtils.d("PhotoPicker selected ${uris.size} items")

        val popup = _popupSelectImage ?: return@registerForActivityResult
        val existingPaths = popup.getImages().map { it.localPath }.toSet()
        val newImages = uris
            .map { UriUtils.uri2File(it).absolutePath }
            .filter { it !in existingPaths }
            .map { path -> Image(billID = mBill.id).apply { localPath = path } }
        popup.setImages((popup.getImages() + newImages).toMutableList())
    }

    private fun setupImagePicker() {
        binding.imgTicket.setOnClickListener {
            XPopup.Builder(requireContext())
                .asCustom(popupSelectImage)
                .show()
        }
    }

    private fun setupKeyboard() {
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

    private fun setupDatePicker() {
        binding.tvBillTime.setOnClickListener {
            val yearMonth = YearMonth.format(mBill.time)
            val onDateSetListener =
                OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    val selectCalendar = mBill.time.calendar()
                    selectCalendar[year, month] = dayOfMonth
                    binding.tvBillTime.text = selectCalendar.time.string()
                    mBill.time = selectCalendar.time
                    selectHourAndMinute(
                        year = year,
                        month = month + 1,
                        dayOfMonth = dayOfMonth,
                        hourOfDay = selectCalendar[Calendar.HOUR_OF_DAY],
                        minute = selectCalendar[Calendar.MINUTE]
                    )
                }
            DatePickerDialog(
                mainActivity,
                onDateSetListener,
                yearMonth.year,
                yearMonth.month - 1,
                yearMonth.day
            ).show()
        }
    }

    // endregion

    // region 账单数据回写（编辑模式）

    /**
     * 编辑模式下回写所有账单字段到 UI
     */
    private fun restoreBillFields() {
        updateTimeDisplay(mBill.time)
        mBill.category?.let { setSelectCategory(it, mBill.type) }
        val tabIndex = if (mBill.type == BillType.EXPENDITURE.value) 0 else 1
        binding.tab.post { binding.tab.getTabAt(tabIndex)?.select() }
        restoreMoney(mBill.money)
        binding.apply {
            tvBillTime.text = mBill.time.string()
            mBill.remark?.let { eidtRemark.setText(it) }
            if (mBill.images.isNotEmpty()) {
                imgTicket.text = "图片(x${mBill.images.size})"
            }
        }
        viewModel.getImages(mBill.images)
    }

    private fun updateTimeDisplay(time: Date) {
        mBill.time = time
        binding.tvBillTime.text = time.string()
        setupDatePicker()
    }

    private fun restoreMoney(money: BigDecimal) {
        val text = money.toPlainString().removeSuffix(".00")
        text.forEach { binding.keyboard.input(it.toString()) }
        binding.tvMoney.text = mBill.money.toString()
    }

    /**
     * 修改时预先选中类别
     */
    private fun setSelectCategory(category: String, type: Int) {
        binding.vpContent.post {
            val index = if (type == BillType.EXPENDITURE.value) 0 else 1
            val categoryFragment = pagerAdapter.getFragment(index) as CategoryFragment
            categoryFragment.setSelectCategory(category)
        }
    }

    // endregion

    // region 分类与类型

    private fun applyCategories(type: Int, categories: MutableList<Category>) {
        val index = if (type == BillType.EXPENDITURE.value) 0 else 1
        val categoryFragment = pagerAdapter.getFragment(index) as CategoryFragment
        categoryFragment.setCategories(categories)
        val billType = BillType.fromValue(type)
        binding.keyboard.setType(billType)
        val color = if (billType == BillType.EXPENDITURE) R.color.expenditure else R.color.income
        binding.tvMoney.setTextColor(resources.getColor(color, null))
    }

    fun selectedCategory(type: Int, category: Category?) {
        mBill.type = BillType.fromValue(type).value
        mBill.category = category?.name
    }

    // endregion

    // region 时间选择

    private fun selectHourAndMinute(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hourOfDay: Int,
        minute: Int
    ) {
        TimePickerDialog(
            mainActivity,
            { _: TimePicker?, h: Int, m: Int ->
                if (h == 0 && m == 0) return@TimePickerDialog
                val selectTime = "$year-$month-$dayOfMonth $h:$m:00"
                updateTimeDisplay(DateConverters.str2Date(selectTime))
            },
            hourOfDay,
            minute,
            true
        ).show()
    }

    // endregion

    // region 保存

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
            mBill.money = BigDecimal(binding.tvMoney.text.toString())
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

    private fun resetForNewBill() {
        mBill = Bill()
        binding.keyboard.clear()
        binding.eidtRemark.setText("")
        binding.tvMoney.text = "0"
        popupSelectImage.clear()
    }

    // endregion

}