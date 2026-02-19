package com.hao.heji.ui.popup;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.tabs.TabLayout;
import com.lxj.xpopup.core.CenterPopupView;
import com.hao.heji.R;
import com.hao.heji.databinding.PopLayoutYearMonthBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.google.android.material.tabs.TabLayout.MODE_AUTO;

/**
 * @date: 2020/11/3
 * @author: 锅得铁
 * #
 */
public class YearSelectPopup extends CenterPopupView implements View.OnClickListener {

    private final OnTabSelected onTabSelected;
    final List<Integer> years;
    int earliestYear = 2016;
    private PopLayoutYearMonthBinding binding;
    private int selectYear;
    private final boolean showAllYear;

    public YearSelectPopup(@NonNull Context context, OnTabSelected onTabSelected, boolean showAllYear) {
        super(context);
        this.onTabSelected = onTabSelected;
        Calendar calendar = Calendar.getInstance();
        years = new ArrayList<>();
        for (int i = calendar.get(Calendar.YEAR); i >= earliestYear; i--) {
            years.add(i);
        }
        this.showAllYear = showAllYear;
        LogUtils.d(years);
    }


    @Override
    protected void onCreate() {
        super.onCreate();
        //root CardView id binding
        binding = PopLayoutYearMonthBinding.bind(getPopupContentView().findViewById(R.id.pop_card_year_month));
        binding.y1.setOnClickListener(this);
        binding.y2.setOnClickListener(this);
        binding.y3.setOnClickListener(this);
        binding.y4.setOnClickListener(this);
        binding.y5.setOnClickListener(this);
        binding.y6.setOnClickListener(this);
        binding.y7.setOnClickListener(this);
        binding.y8.setOnClickListener(this);
        binding.y9.setOnClickListener(this);
        binding.y10.setOnClickListener(this);
        binding.y11.setOnClickListener(this);
        binding.y12.setOnClickListener(this);
        binding.btnYearAll.setOnClickListener(v -> {
            onTabSelected.selected(selectYear, 0);
            dismiss();
        });
        if (showAllYear) {
            binding.btnYearAll.setVisibility(View.VISIBLE);
        } else {
            binding.btnYearAll.setVisibility(View.GONE);
        }
        initYearsTab();
    }

    private void initYearsTab() {
        years.forEach(s -> {
            TabLayout.Tab yearTab = binding.tabYears.newTab();
            yearTab.setText(String.valueOf(s));
            binding.tabYears.addTab(yearTab);
        });
        binding.tabYears.getTabAt(0).select();
        selectYear = Integer.parseInt(binding.tabYears.getTabAt(0).getText().toString());
        binding.tabYears.setTabMode(MODE_AUTO);
        binding.tabYears.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ToastUtils.showLong(tab.getText());
                selectYear = Integer.parseInt(tab.getText().toString());
//                if (null != onTabSelected) {
//                    onTabSelected.selected(selectYear, 0);
//                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        if (null != onTabSelected) {
            onTabSelected.selected(selectYear, Integer.valueOf((String) v.getTag()));
            dismiss();
        }
    }

    public interface OnTabSelected {
        void selected(int year, int month);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_layout_year_month;
    }
}
