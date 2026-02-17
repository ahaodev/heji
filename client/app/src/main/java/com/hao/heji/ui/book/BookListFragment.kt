package com.hao.heji.ui.book

import android.graphics.Rect
import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.XPopup
import com.hao.heji.config.Config
import com.hao.heji.R
import com.hao.heji.data.Result
import com.hao.heji.data.db.Book
import com.hao.heji.databinding.FragmentBookListBinding
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.base.hideRefreshing
import com.hao.heji.ui.base.swipeRefreshLayout
import kotlinx.coroutines.runBlocking

import androidx.recyclerview.widget.DiffUtil.ItemCallback as ItemCallback

/**
 * @date: 2021/7/9
 * @author: 锅得铁
 * #
 */
class BookListFragment : BaseFragment() {
    lateinit var adapter: BookListAdapter
    private val  binding: FragmentBookListBinding by lazy {
        FragmentBookListBinding.inflate(layoutInflater)
    }
    private val bookViewModel: BookViewModel by koinViewModel()

    override fun layout()=binding.root

    override fun setUpToolBar() {
        super.setUpToolBar()
        toolBar.title = "账本"
        showBlack()
    }

    override fun initView(rootView: View) {
        adapter = BookListAdapter {
            findNavController().navigate(
                R.id.nav_book_setting,
                BookSettingFragmentArgs(it).toBundle()
            )
        }
        val diffItemCallBack = object : ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return diff(oldItem, newItem)
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return diff(oldItem, newItem)
            }

            private fun diff(
                oldItem: Book,
                newItem: Book,
            ): Boolean {
                val idSame = oldItem.id == newItem.id
                val nameSame = oldItem.name == newItem.name
                val typeSame = oldItem.type == newItem.type
                return idSame && nameSame && typeSame
            }
        }
        adapter.setDiffCallback(diffItemCallBack)
        adapter.recyclerView = binding.recycler
        binding.recycler.layoutManager = LinearLayoutManager(mainActivity)
        binding.recycler.adapter = adapter
        binding.recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                outRect.left = 16
                outRect.top = 16
                outRect.bottom = 16
                outRect.right = 16
                //super.getItemOffsets(outRect, view, parent, state)
            }
        })
//        adapter.animationEnable = true
//        adapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight)
        swipeRefreshLayout(binding.refreshLayout) { bookViewModel.getBookList() }
        bookViewModel.bookList().observe(this) {
            adapter.setDiffNewData(it)
            hideRefreshing(binding.refreshLayout)
        }
        listener()
    }

    private fun listener() {
        adapter.setOnItemClickListener { adapter, view, position ->
            val book: Book = adapter.getItem(position) as Book
            mainActivity.setCurrentBook(book.name)
            runBlocking {
                Config.setBook(book)
            }
            findNavController().popBackStack()
        }
        binding.fab.setOnClickListener {
            XPopup.Builder(requireContext()).asBottomList(
                "", arrayOf("新建账本", "加入他人账本")
            ) { _, text ->
                when (text) {
                    "新建账本" ->
                        findNavController().navigate(R.id.nav_book_add)
                    "加入他人账本" -> {
                        showJoinBookPop()
                    }

                }

            }.show()
        }
    }

    private fun showJoinBookPop() {
        XPopup.Builder(requireContext())
            .hasStatusBarShadow(false)
            .autoOpenSoftInput(true)
            .asInputConfirm("请输入账本邀请码", "") { text ->
                bookViewModel.joinBook(text) {
                    when (it) {
                        is Result.Success -> {
                            bookViewModel.getBookList()
                        }
                        is Result.Error -> {
                            ToastUtils.showLong(it.exception.message)
                        }
                        Result.Loading -> null
                    }
                }
            }.show()

    }
}