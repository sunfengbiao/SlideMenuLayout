# SlideMenuLayout
Android进阶之旅-->自定义view-->酷狗侧滑菜单
# View绘制流程:
ViewRootImpl: requestLayout()-->performTraversals()<测量，摆放，绘制>
## 1.测量：
performMeasure()-->用于制定和测量layout中所有控件的宽高，对于ViewGroup,先去测量里面的子孩子，根据自孩子的宽高计算自己的宽高，对与View，它的宽高有自己和父布局共同决定（测量模式）。 由外到里，再由里到外

## 2.摆放：
performLayout()-->用于摆放子布局，for循环所有子view,用child.layout()摆放ChildView 。 单选循环，由外到里

## 3.绘制：
performDraw()-->用于绘制自己还有子view,对于GroupView先绘制自己的背景，for循环绘制子View,调用子view的draw()方法，对于view绘制自己的背景，绘制自己显示的内容（如：TextView 先绘制背景，再cavas.drawText()文本）



## 源码调用规律总结：
### performMeasure（）-->measure（）-->onMeasure()
### performLayout（）-->layout（）-->onLayout()
### performDraw（）-->draw-->onDraw()
