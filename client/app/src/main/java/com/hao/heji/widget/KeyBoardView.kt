package com.hao.heji.widget;

import static java.math.BigDecimal.ZERO;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hao.heji.R;
import com.hao.heji.data.BillType;
import com.hao.heji.databinding.LayoutKeyboardBinding;

import java.math.BigDecimal;
import java.util.Stack;

/**
 * @date: 2020/11/19
 * @author: 锅得铁
 * #
 */
public class KeyBoardView extends ConstraintLayout {
    public static final String TAG = "KeyBoardView";
    public static final int INPUT_MAXSIZE = 12;//输入值最大限制
    String defValue = "0";
    private final Context context;
    LayoutKeyboardBinding binding;
    private Stack<String> stack = new Stack<>();
    private OnKeyboardListener keyboardListener;

    public void setKeyboardListener(OnKeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;
    }


    public KeyBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_keyboard, this);
        binding = LayoutKeyboardBinding.bind(view);
        initKeyboardListener(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initKeyboardListener(View view) {
        binding.k0.setOnClickListener(k0 -> {
            input("0");
        });
        binding.k1.setOnClickListener(k1 -> {
            input("1");
        });
        binding.k2.setOnClickListener(k2 -> {
            input("2");
        });
        binding.k3.setOnClickListener(k3 -> {
            input("3");
        });
        binding.k4.setOnClickListener(k4 -> {
            input("4");
        });
        binding.k5.setOnClickListener(k5 -> {
            input("5");
        });
        binding.k6.setOnClickListener(k6 -> {
            input("6");
        });
        binding.k7.setOnClickListener(k7 -> {
            input("7");
        });
        binding.k8.setOnClickListener(k8 -> {
            input("8");
        });
        binding.k9.setOnClickListener(k9 -> {
            input("9");
        });
        binding.kPoint.setOnClickListener(point -> {
            input(".");
        });
        binding.ksub.setOnClickListener(minus -> {//减
            input("-");
        });
        binding.ksum.setOnClickListener(plus -> {//加
            input("+");
        });

        binding.kDelete.setOnClickListener(delete -> {
            delete();
        });

        binding.kSaveAgain.setOnClickListener(new ClickUtils.OnDebouncingClickListener(500) {
            @Override
            public void onDebouncingClick(View v) {
                if (keyboardListener != null)
                    keyboardListener.saveAgain(finalCompute());
            }
        });
        binding.kSave.setOnClickListener(new ClickUtils.OnDebouncingClickListener() {
            @Override
            public void onDebouncingClick(View v) {
                if (keyboardListener != null)
                    keyboardListener.save(finalCompute());
            }
        });
    }

    public String getValue() {
        return defValue;
    }


    public void setType(BillType billType) {
        int color = (billType == BillType.EXPENDITURE) ? R.color.expenditure : R.color.income;
        int drawable = (billType == BillType.EXPENDITURE) ? R.drawable.keyboard_save_bg_red : R.drawable.keyboard_save_bg_green;
        binding.kSave.setBackground(context.getDrawable(drawable));
        binding.kSave.setTextColor(context.getColor(R.color.white));
        invalidate();
    }

    public interface OnKeyboardListener {
        void save(String result);

        void calculation(String result);

        void saveAgain(String result);
    }

    public void input(String input) {
        heardClear0(input);//头部抹0
        heardPM();//头部抹去+-.
        if (stack.isEmpty() && inputIsSymbol(input)) {//空值情况输入 + - .符号无效,可以输入0.00小数字
            return;
        }

        if (!stack.isEmpty()) {//栈内存在输入

            if (!stack.isEmpty() && lastIsSymbol() && inputIsSymbol(input)) {//末尾为运算符||小数点
                if (stack.lastElement().equals(input)) {//末尾和输入的符号一致
                    return;
                }
                stack.pop();//不一致，先pop 再push
            }

            if (input.equals(".")) {//输入为小数点
                if (stack.size() == 1) {//运算结果包含.小数点
                    String firstElement = stack.firstElement();
                    if (firstElement.contains(".")) return;//禁止输入
                }
                if (stack.contains(".")) {//栈内包含点
                    int pointCount = (int) stack.stream().filter(s -> s.equals(".")).count();//遍历栈内小数点个数
                    if (stack.firstElement().contains(".")) pointCount += 1;//栈的第一个可能为运算后的小数 小数点+1
                    if (stack.contains("+") || stack.contains("-")) {//+ - 号前如果有点，最多允许后面再有一个点
                        if (pointCount > 1) return;
                    } else {
                        if (pointCount > 0) return;
                    }
                }
            }

            if (inputIsOperator(input)) {//输入为运算符+ —
                if (stack.contains("+") || stack.contains("-")) {//栈内已经包含了符号
                    StringBuilder sb = new StringBuilder();
                    stack.forEach(s -> {
                        sb.append(s);
                    });
                    String value = sb.toString();
                    String request = compute(value);//计算一次+ -结果
                    stack.clear();//清空栈

                    stack.push(request);//结果入栈
                }
            }
            if (!inputIsSymbol(input)) {//输入为正常数字
                if (stack.size() > 3) {
                    int index = stack.size() - 3;//避免输入3位小数
                    if (stack.get(index).equals(".")) return;
                }
                if (stack.size() == 1) {//避免运算结果直接在后面继续输入
                    String firstElement = stack.firstElement();
                    if (firstElement.length() > 2) return;//禁止输入
                }
            }
        }


        if (stack.size() > INPUT_MAXSIZE) {
            ToastUtils.showShort("输入数值太大了");
            return;
        }
        stack.push(input);
        request();
    }

    /**
     * 一开始输入0的情况直接抹掉
     */
    private void heardClear0(String input) {
        if (input.equals(".")) return;//小数点
        if (!stack.isEmpty() && stack.size() == 1) {
            if (stack.firstElement().equals("0")) {
                stack.remove(0);//抹去头部
                heardClear0(input);
            }
        }
    }

    /**
     * 一开始输入+ —的情况直接抹掉
     */
    private void heardPM() {
        if (!stack.isEmpty() && stack.size() > 0) {
            if (stack.firstElement().equals("-") || stack.firstElement().equals("+")) {
                stack.remove(0);//抹去头部
                heardPM();
            }
        }
    }

    /**
     * 栈顶为符号
     *
     * @return
     */
    private boolean lastIsSymbol() {
        String last = stack.lastElement();
        boolean stackTopSymbol = last.equals("+") || last.equals("-") || last.equals(".");
        return stackTopSymbol;
    }

    /**
     * 输入为运算符
     *
     * @return
     */
    private boolean inputIsOperator(String input) {
        boolean inputSymbol = input.equals("+") || input.equals("-");
        return inputSymbol;
    }

    /**
     * 输入的是符号
     *
     * @param input
     * @return
     */
    private boolean inputIsSymbol(String input) {
        boolean inputSymbol = input.equals("+") || input.equals("-") || input.equals(".");
        return inputSymbol;
    }

    public void delete() {
        if (!stack.isEmpty()) {
            stack.pop();
        }
        request();
    }

    private String request() {
        if (stack.isEmpty()) {
            if (keyboardListener != null)
                keyboardListener.calculation(defValue);
            return defValue;
        }
        StringBuffer sb = new StringBuffer();
        stack.forEach(s -> sb.append(s));
        String request = sb.toString();
        if (keyboardListener != null)
            keyboardListener.calculation(request);
        return request;
    }

    /**
     * 计算栈内的值
     *
     * @param value
     * @return 返回最终结果
     */
    private String compute(String value) {
        BigDecimal request = null;
        if (value.contains("+")) {
            String v1 = value.substring(0, value.indexOf("+"));
            String v2 = value.substring(value.indexOf("+") + 1);
            BigDecimal f1 = new BigDecimal(v1);
            BigDecimal f2 = new BigDecimal(v2);
            request = f1.add(f2);
        }
        if (value.contains("-")) {
            String v1 = value.substring(0, value.indexOf("-"));
            String v2 = value.substring(value.indexOf("-") + 1);
            BigDecimal f1 = new BigDecimal(v1);
            BigDecimal f2 = new BigDecimal(v2);
            request = f1.subtract(f2);
        }
        if (request.longValue() < 0) {
            request = ZERO;//为负数时置0
        }
        String lastCompute = request.stripTrailingZeros().toPlainString();//抹去.00
        return lastCompute;
    }

    /**
     * 最终运算
     */
    private String finalCompute() {
        if (stack.isEmpty()) return defValue;
        if (lastIsSymbol()) stack.pop();
        StringBuilder sb = new StringBuilder();
        stack.forEach(s -> {
            sb.append(s);
        });
        if (stack.contains("+") || stack.contains("-")) {//栈内已经包含了符号
            String value = sb.toString();
            String request = compute(value);
            stack.clear();
            stack.push(request);
            return request();
        } else {
            return new BigDecimal(sb.toString()).toString();
        }
    }

    public Stack<String> getStack() {
        return stack;
    }

    public void setStack(Stack<String> stack) {
        this.stack = stack;
        request();
    }

    public void clear() {
        stack.clear();
        defValue = "0";
    }
}
