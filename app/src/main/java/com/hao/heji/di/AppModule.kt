package com.hao.heji.di

import com.hao.heji.AppViewModel
import com.hao.heji.data.repository.BookRepository
import com.hao.heji.data.repository.CategoryRepository
import com.hao.heji.data.repository.ImageRepository
import com.hao.heji.data.repository.UserRepository
import com.hao.heji.network.HttpManager
import com.hao.heji.ui.MainViewModel
import com.hao.heji.ui.book.BookViewModel
import com.hao.heji.ui.calendar.CalendarNoteViewModule
import com.hao.heji.ui.category.manager.CategoryManagerViewModel
import com.hao.heji.ui.create.CreateBillViewModel
import com.hao.heji.ui.home.BillListViewModel
import com.hao.heji.ui.report.ReportViewModel
import com.hao.heji.ui.setting.SettingViewModel
import com.hao.heji.ui.setting.banner.BannerViewModel
import com.hao.heji.ui.setting.export.ExportViewModel
import com.hao.heji.ui.setting.input.etc.ETCViewModel
import com.hao.heji.ui.user.info.UserInfoViewModel
import com.hao.heji.ui.user.login.LoginViewModel
import com.hao.heji.ui.user.register.RegisterViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single { HttpManager() }
}

val repositoryModule = module {
    factory { BookRepository(get()) }
    factory { UserRepository(get()) }
    factory { CategoryRepository(get()) }
    factory { ImageRepository() }
}

val viewModelModule = module {
    single { AppViewModel(androidApplication()) }
    viewModel { MainViewModel(get()) }
    viewModel { BillListViewModel() }
    viewModel { BookViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { CategoryManagerViewModel() }
    viewModel { CreateBillViewModel() }
    viewModel { ReportViewModel() }
    viewModel { CalendarNoteViewModule() }
    viewModel { ExportViewModel(get()) }
    viewModel { ETCViewModel() }
    viewModel { SettingViewModel() }
    viewModel { UserInfoViewModel() }
    viewModel { BannerViewModel() }
}

val appModules = listOf(networkModule, repositoryModule, viewModelModule)
