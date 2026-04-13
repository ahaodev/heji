package com.hao.heji.ui.setting.input.etc

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.R
import com.hao.heji.config.Config
import com.hao.heji.databinding.FragmentEtcBinding
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.base.render
import com.lxj.xpopup.XPopup
import java.util.Calendar

/**
 * @date: 2020/10/27
 * @author: 锅得铁ø
 * #
 */
class ETCFragment : BaseFragment() {
    private val binding: FragmentEtcBinding by lazy { FragmentEtcBinding.inflate(layoutInflater) }
    private val etcViewModel: ETCViewModel by koinViewModel()
    private val inputLoading by lazy {
        XPopup.Builder(requireContext()).asLoading().setTitle("正在导入")
    }

    override fun onResume() {
        super.onResume()
        //拦截回退直接退出  object : Class 内部类
        registerBackPressed {
            if (binding.etcWeb.canGoBack()) {
                binding.etcWeb.goBack()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    override fun initView(rootView: View) {
        val webSettings = binding.etcWeb.settings
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.allowFileAccessFromFileURLs = true
        // Enable remote debugging via chrome://inspect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        binding.etcWeb.loadUrl(ETC.URL)
        val client: WebViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view1: WebView,
                request: WebResourceRequest
            ): Boolean {
                return super.shouldOverrideUrlLoading(view1, request)
            }

            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? { //拦截请求
                //拦截的URL "http://hubeiweixin.u-road.com/HuBeiCityAPIServer/index.php/huibeicityserver/getCardMonthStatInfo"
                //关键字
                val key = "showetcacount"
                //获取参数
                val requestUrl = request.requestHeaders["Referer"]
                if (!TextUtils.isEmpty(requestUrl) && requestUrl!!.contains(key)) {
                    getParameters(requestUrl)
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view1: WebView, url: String) { //加载完成
                super.onPageFinished(view1, url)
                LogUtils.d(url)
                val showetcsearch = "showetcsearch" //查询页面
                val showetcacountdetail = "showetcacountdetail" //详情列表
                val showetcacount = "showetcacount" //月统计页
                if (url.contains(showetcsearch)) {
                    inputValue(view1) //自动填充Value
                } else if (url.contains(showetcacountdetail)) {
                    //http://hubeiweixin.u-road.com/HuBeiCityAPIServer/index.php/huibeicityserver/showetcacountdetail/42021909230571219224/2020-09/%E9%84%82FNA518
                    getParameters(url)
                } else if (url.contains(showetcacount)) {
                    //http://hubeiweixin.u-road.com/HuBeiCityAPIServer/index.php/huibeicityserver/showetcacount/42021909230571219224/202009/%E9%84%82FNA518
                    getParameters(url)
                }
            }

            private fun getParameters(url: String?) {
                val ps = url!!.split("/").toTypedArray()
                val carID = ps[ps.size - 1]
                var yearMonth = ps[ps.size - 2]
                val etcID = ps[ps.size - 3]
                if (!yearMonth.contains("-")) {
                    val year = yearMonth.substring(0, 4)
                    val month = yearMonth.substring(year.length)
                    yearMonth = "$year-$month"
                }
                etcViewModel.carID = carID
                etcViewModel.etcID = etcID
                etcViewModel.yearMonth = yearMonth
            }
        }
        binding.etcWeb.webViewClient = client

        render(etcViewModel) {
            when (it) {
                is ETCUiState.InputSuccess -> {
                    inputLoading.setTitle("导入成功")
                    Handler(Looper.getMainLooper()).postDelayed(
                        { inputLoading.dismiss() },
                        1000
                    )
                }

                is ETCUiState.InputError -> {
                    ToastUtils.showShort("导入失败")
                }
            }
        }
    }

    override fun layout() = binding.root

    override fun setUpToolBar() {
        super.setUpToolBar()
        showBlack()
        toolBar.title = "ETC账单"
        toolBar.inflateMenu(R.menu.input)
        toolBar.menu.findItem(R.id.item1).title = "导入"
        toolBar.menu.findItem(R.id.item1).setOnMenuItemClickListener {
            requestAlert()
            false
        }
    }

    /**
     * JS输入ETC卡号、车牌号
     */
    private fun inputValue(webView: WebView) {
        val functionInput = "javascript:" +
                " function inputNumber() { " +
                "   document.getElementById(\"cardnum\").value = '" + ETC.ID + "';" +
                "   document.getElementById(\"vehplate\").value = '" + ETC.CAR_ID + "';" +
                "}"
        webView.loadUrl(functionInput) //写入ETC卡号、车牌号
        //执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val stringValueCallback = ValueCallback<String> { value: String? -> LogUtils.d(value) }
            webView.evaluateJavascript("javascript: inputNumber();", stringValueCallback)
        } else {
            webView.loadUrl("javascript: inputNumber();")
        }
    }

    private fun requestAlert() {
        if (TextUtils.isEmpty(etcViewModel.etcID) || TextUtils.isEmpty(etcViewModel.carID) || TextUtils.isEmpty(
                etcViewModel.yearMonth
            )
        ) {
            selectMonth()
            XPopup.Builder(requireContext())
                .asConfirm("导入提示", "请先按月份查询账单") { }
                .show()
        } else {
            XPopup.Builder(requireContext())
                .asConfirm(
                    "导入" + etcViewModel.yearMonth + "账单",
                    "当前账本【${Config.book.name}】，确认导入吗？"
                ) {

                    inputLoading.show()
                    etcViewModel.requestHBGSETCList(
                        etcViewModel.etcID,
                        etcViewModel.yearMonth!!,
                        etcViewModel.carID
                    )
                }
                .show()
        }
    }

    private fun selectMonth() {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val months = arrayOf(
            "$year-01",
            "$year-02",
            "$year-03",
            "$year-04",
            "$year-05",
            "$year-06",
            "$year-07",
            "$year-08",
            "$year-09",
            "$year-10",
            "$year-11",
            "$year-12"
        )
        XPopup.Builder(requireContext())
            .asBottomList("选择月份", months) { position, text -> etcViewModel.yearMonth = text }
            .show()
    }
}