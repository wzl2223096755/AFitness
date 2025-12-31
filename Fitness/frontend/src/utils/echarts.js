/**
 * ECharts 按需导入配置
 * 只导入项目中实际使用的图表组件，减少打包体积
 */
import * as echarts from 'echarts/core'

// 导入图表类型
import {
  LineChart,
  BarChart,
  PieChart,
  RadarChart,
  GaugeChart,
  ScatterChart
} from 'echarts/charts'

// 导入组件
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  ToolboxComponent,
  DataZoomComponent,
  VisualMapComponent,
  MarkLineComponent,
  MarkPointComponent,
  MarkAreaComponent
} from 'echarts/components'

// 导入渲染器
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的组件
echarts.use([
  // 图表类型
  LineChart,
  BarChart,
  PieChart,
  RadarChart,
  GaugeChart,
  ScatterChart,
  // 组件
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  ToolboxComponent,
  DataZoomComponent,
  VisualMapComponent,
  MarkLineComponent,
  MarkPointComponent,
  MarkAreaComponent,
  // 渲染器
  CanvasRenderer
])

export default echarts
