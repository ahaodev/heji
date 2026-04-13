# 合記
> 注：该项目为练手项目仅仅用做学习和非商业化目的，包含了前后端，UI方面参考了钱迹（无广告且记账速度非常快的一款记账软件），我本身也是钱迹的超级终身VIP，非常感谢钱迹的优秀开发者。

## 功能介绍

多人同时记账,账单区分权限,账单统计,账单导入,账单导出，账单同步

### UI
> 账本封面拍摄于2018hetian玉泉湖公园的木阶梯,基本功能完善后添加账本封面自定义功能。

<table>
    <tr>
      <td>
          <img src="./docs/img/home.png" alt="hello" style="zoom:67%;"/>       
      </td>
      <td>
          <img src="./docs/img/booklist.png" alt="hello" alt="hello" style="zoom:67%;"/>
      </td>
      <td>
          <img src="./docs/img/save.png" alt="addBill" style="zoom:67%;"/>
      </td>
    </tr>
    <tr>
        <td><img src="./docs/img/total1.png" alt="hello" style="zoom:67%;"/></td>
        <td> <img src="./docs/img/total2.png" alt="hello" style="zoom:67%;"/></td>
        <td><img src="./docs/img/total3.png" alt="hello" style="zoom:67%;"/></td>
    </tr>
        <tr>
        <td><img src="./docs/img/timeview.png" alt="hello" style="zoom:67%;"/></td>
        <td> <img src="./docs/img/menu.png" alt="hello" style="zoom:67%;"/></td>
        <td><img src="./docs/img/setting.png" alt="hello" style="zoom:67%;"/></td>
    </tr>
</table>


### 账本

#### 功能描述：
1. 用户均可创建账本,账本通过口令分享给其他记账人.仅创建人具有删除账本权限
2. 账本中仅仅可修改删除自己创建的账本
3. 账本具备{账本名称\账本所属类别}
4. 账单有金额、时间、票据、记账人、经手人、类别等属性

#### 账单
1. 账单分为收入\支出
  
2. 账单属性

   > 账单类别|金额|时间|票据图片|备注信息

3. 账单查重功能

   > 通过账单时间\金额\票据MD5值判断是否存在重复记录

4. 账单导入

   >  导入支持 支付宝、微信、ETC 、EXCEL、CVS、钱迹 

5. 账单导出

   > 导出支持 EXCEL、CVS、钱迹
   >
6. 多人记账用户仅能删除或修改自身账单

#### 统计
1. 支出人员支出占比
   * 起始资金占比
   * 月支出占比
   * 年支出占比
   
    > 根据时间分为月/年/有史以来
2. 支出走势
   * 不同类别支出走势图
3. 收支类型占比
   * 全年收支占比
   * 月收支占比
4. 报表
   * 全年收支报表
   * 月收支报表
5. 收支总览
   * 年收支总揽
   * 月收支总揽

## Client Android

Android client 采用单Activity 多Fragment的MVI模式

### 技术栈
*   基于AndroidX,使用Java + kotlin 混合开发
*   OkHttp + retrofit 网络请求
*   navigation Fragment导航
*   BaseRecyclerViewAdapterHelper 列表的展示
*   moshi 数据格式化
*   MatisseKotlin 图片选择
*   permissionx 人性化的权限封装
*   room 更简单好调试的Sqlite数据库
*   utilcode 强大简单的工具集
*   xpopup 多样式的弹窗
*   Luban 账单图片压缩
*   calendarview 日历账单视图与记账
*   datastore  少量数据的本地存储
*   MPAndroidChart 强大的图表用做统计
*   immersionbar 多机型Toobar的封装适配

## 服务端
> 正在构建...
