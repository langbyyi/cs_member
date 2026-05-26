# 会员等级工具使用说明

## 概述

会员等级工具 (`memberGrade.ts`) 提供了完整的会员等级计算、显示和管理功能。

## 核心功能

### 1. MemberGradeUtil 工具类

主要方法：

- `getGradeConfigs()` - 获取等级配置列表
- `calculateGrade(points)` - 根据积分计算会员等级
- `calculateGradeSync(points)` - 同步计算等级（不等待异步加载）
- `getGradeClass(gradeCode)` - 获取等级CSS类名
- `getGradeProgress()` - 获取等级进度百分比
- `getPointsToNextLevel()` - 获取升级所需积分
- `preloadGradeConfigs()` - 预加载等级配置

### 2. 便捷函数

- `getMemberGrade(points)` - 计算会员等级
- `getGradeClass(gradeCode)` - 获取等级样式类
- `getGradeProgress()` - 获取等级进度

## 使用示例

### 基础使用

```typescript
import { getMemberGrade, getGradeClass } from '@/utils/memberGrade'

// 计算会员等级
const grade = await getMemberGrade(2500)
console.log(grade.gradeName) // "VIP2会员"
console.log(grade.color) // "#87CEEB"

// 获取CSS类名
const cssClass = getGradeClass('VIP2')
console.log(cssClass) // "grade-vip2"
```

### 在Vue组件中使用

```vue
<template>
  <div>
    <!-- 使用等级徽章组件 -->
    <MemberGradeBadge
      :member-points="totalPoints"
      :show-progress="true"
    />

    <!-- 自定义等级显示 -->
    <div
      v-if="currentGrade"
      class="grade-badge"
      :class="getGradeClass(currentGrade.gradeCode)"
      :style="{ backgroundColor: currentGrade.color }"
    >
      {{ currentGrade.gradeName }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMemberGrade, MemberGradeUtil } from '@/utils/memberGrade'
import MemberGradeBadge from '@/components/MemberGradeBadge.vue'

const totalPoints = ref(0)
const currentGrade = ref(null)

onMounted(async () => {
  // 预加载等级配置（可选）
  await MemberGradeUtil.preloadGradeConfigs()

  // 计算当前会员等级
  currentGrade.value = await getMemberGrade(totalPoints.value)
})
</script>
```

### 应用启动时预加载

```typescript
// main.ts 或 app.vue
import { MemberGradeUtil } from '@/utils/memberGrade'

// 应用启动时预加载等级配置
MemberGradeUtil.preloadGradeConfigs().then(() => {
  console.log('等级配置预加载完成')
})
```

## 等级配置

等级配置从后端 `/api/v1/admin/member-grade/public/list` 接口获取，包含：

- VIP1: 0-999积分 (#C0C0C0)
- VIP2: 1000-2999积分 (#87CEEB)
- VIP3: 3000-5999积分 (#FFD700)
- VIP4: 6000-9999积分 (#FF8C00)
- VIP5: 10000-19999积分 (#FF6347)
- VIP6: 20000-49999积分 (#9370DB)
- VIP7: 50000+积分 (#FF1493)

## 组件使用

### MemberGradeBadge 组件

Props:
- `memberPoints` (必填): 会员累计积分
- `showProgress` (可选): 是否显示进度条，默认 true
- `autoLoad` (可选): 是否自动加载等级配置，默认 true

使用示例：
```vue
<MemberGradeBadge
  :member-points="2500"
  :show-progress="true"
/>
```

## CSS样式

工具类会自动生成 `grade-{gradeCode}` CSS类名，例如：
- `grade-vip1`
- `grade-vip2`
- `grade-vip3`
- ...

## 错误处理

工具类包含完整的错误处理机制：
- 如果后端接口失败，会使用默认等级配置
- 如果积分计算异常，会返回最低等级
- 所有异步操作都有错误日志记录

## 性能优化

- 等级配置会缓存在内存中，避免重复请求
- 提供同步方法 `calculateGradeSync` 避免异步等待
- 支持预加载配置，提高首次计算速度

## 注意事项

1. 等级配置是从后端动态获取的，确保相关接口正常工作
2. 建议在应用启动时调用 `preloadGradeConfigs()` 预加载配置
3. 使用 `calculateGradeSync` 方法时，确保配置已经加载完成
4. CSS样式可以根据实际需求自定义