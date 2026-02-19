package com.hao.heji.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.R
import com.hao.heji.data.BillType
import com.hao.heji.databinding.LayoutKeyboardBinding
import java.math.BigDecimal
import java.util.Stack

class KeyBoardView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    companion object {
        const val TAG = "KeyBoardView"
        const val INPUT_MAXSIZE = 12
    }

    var defValue = "0"
        private set

    private val binding: LayoutKeyboardBinding
    var stack: Stack<String> = Stack()
        set(value) {
            field = value
            request()
        }

    var keyboardListener: OnKeyboardListener? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_keyboard, this)
        binding = LayoutKeyboardBinding.bind(view)
        initKeyboardListener()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun initKeyboardListener() {
        binding.k0.setOnClickListener { input("0") }
        binding.k1.setOnClickListener { input("1") }
        binding.k2.setOnClickListener { input("2") }
        binding.k3.setOnClickListener { input("3") }
        binding.k4.setOnClickListener { input("4") }
        binding.k5.setOnClickListener { input("5") }
        binding.k6.setOnClickListener { input("6") }
        binding.k7.setOnClickListener { input("7") }
        binding.k8.setOnClickListener { input("8") }
        binding.k9.setOnClickListener { input("9") }
        binding.kPoint.setOnClickListener { input(".") }
        binding.ksub.setOnClickListener { input("-") }
        binding.ksum.setOnClickListener { input("+") }

        binding.kDelete.setOnClickListener { delete() }

        binding.kSaveAgain.setOnClickListener(object : ClickUtils.OnDebouncingClickListener(500) {
            override fun onDebouncingClick(v: View) {
                keyboardListener?.saveAgain(finalCompute())
            }
        })
        binding.kSave.setOnClickListener(object : ClickUtils.OnDebouncingClickListener() {
            override fun onDebouncingClick(v: View) {
                keyboardListener?.save(finalCompute())
            }
        })
    }

    fun getValue(): String = defValue

    fun setType(billType: BillType) {
        val color = if (billType == BillType.EXPENDITURE) R.color.expenditure else R.color.income
        val drawable = if (billType == BillType.EXPENDITURE) R.drawable.keyboard_save_bg_red else R.drawable.keyboard_save_bg_green
        binding.kSave.background = context.getDrawable(drawable)
        binding.kSave.setTextColor(context.getColor(R.color.white))
        invalidate()
    }

    interface OnKeyboardListener {
        fun save(result: String)
        fun calculation(result: String)
        fun saveAgain(result: String)
    }

    fun input(input: String) {
        heardClear0(input)
        heardPM()
        if (stack.isEmpty() && inputIsSymbol(input)) {
            return
        }

        if (stack.isNotEmpty()) {
            if (lastIsSymbol() && inputIsSymbol(input)) {
                if (stack.lastElement() == input) {
                    return
                }
                stack.pop()
            }

            if (input == ".") {
                if (stack.size == 1) {
                    val firstElement = stack.firstElement()
                    if (firstElement.contains(".")) return
                }
                if (stack.contains(".")) {
                    var pointCount = stack.count { it == "." }
                    if (stack.firstElement().contains(".")) pointCount += 1
                    if (stack.contains("+") || stack.contains("-")) {
                        if (pointCount > 1) return
                    } else {
                        if (pointCount > 0) return
                    }
                }
            }

            if (inputIsOperator(input)) {
                if (stack.contains("+") || stack.contains("-")) {
                    val sb = StringBuilder()
                    stack.forEach { s -> sb.append(s) }
                    val value = sb.toString()
                    val result = compute(value)
                    stack.clear()
                    stack.push(result)
                }
            }
            if (!inputIsSymbol(input)) {
                if (stack.size > 3) {
                    val index = stack.size - 3
                    if (stack[index] == ".") return
                }
                if (stack.size == 1) {
                    val firstElement = stack.firstElement()
                    if (firstElement.length > 2) return
                }
            }
        }

        if (stack.size > INPUT_MAXSIZE) {
            ToastUtils.showShort("输入数值太大了")
            return
        }
        stack.push(input)
        request()
    }

    private fun heardClear0(input: String) {
        if (input == ".") return
        if (stack.isNotEmpty() && stack.size == 1) {
            if (stack.firstElement() == "0") {
                stack.removeAt(0)
                heardClear0(input)
            }
        }
    }

    private fun heardPM() {
        if (stack.isNotEmpty() && stack.size > 0) {
            if (stack.firstElement() == "-" || stack.firstElement() == "+") {
                stack.removeAt(0)
                heardPM()
            }
        }
    }

    private fun lastIsSymbol(): Boolean {
        val last = stack.lastElement()
        return last == "+" || last == "-" || last == "."
    }

    private fun inputIsOperator(input: String): Boolean {
        return input == "+" || input == "-"
    }

    private fun inputIsSymbol(input: String): Boolean {
        return input == "+" || input == "-" || input == "."
    }

    fun delete() {
        if (stack.isNotEmpty()) {
            stack.pop()
        }
        request()
    }

    private fun request(): String {
        if (stack.isEmpty()) {
            keyboardListener?.calculation(defValue)
            return defValue
        }
        val sb = StringBuilder()
        stack.forEach { s -> sb.append(s) }
        val result = sb.toString()
        keyboardListener?.calculation(result)
        return result
    }

    private fun compute(value: String): String {
        var result: BigDecimal? = null
        if (value.contains("+")) {
            val v1 = value.substring(0, value.indexOf("+"))
            val v2 = value.substring(value.indexOf("+") + 1)
            val f1 = BigDecimal(v1)
            val f2 = BigDecimal(v2)
            result = f1.add(f2)
        }
        if (value.contains("-")) {
            val v1 = value.substring(0, value.indexOf("-"))
            val v2 = value.substring(value.indexOf("-") + 1)
            val f1 = BigDecimal(v1)
            val f2 = BigDecimal(v2)
            result = f1.subtract(f2)
        }
        if (result!!.toLong() < 0) {
            result = BigDecimal.ZERO
        }
        return result.stripTrailingZeros().toPlainString()
    }

    private fun finalCompute(): String {
        if (stack.isEmpty()) return defValue
        if (lastIsSymbol()) stack.pop()
        val sb = StringBuilder()
        stack.forEach { s -> sb.append(s) }
        return if (stack.contains("+") || stack.contains("-")) {
            val value = sb.toString()
            val result = compute(value)
            stack.clear()
            stack.push(result)
            request()
        } else {
            BigDecimal(sb.toString()).toString()
        }
    }

    fun clear() {
        stack.clear()
        defValue = "0"
    }
}
