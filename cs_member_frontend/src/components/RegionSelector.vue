<template>
  <div class="region-selector">
    <!-- 省份选择 -->
    <el-form-item :label="provinceLabel" prop="province">
      <el-select
        v-model="selectedProvince"
        :placeholder="provincePlaceholder"
        filterable
        :filter-method="filterProvinces"
        @change="handleProvinceChange"
        @clear="handleClearProvince"
        clearable
        :disabled="disabled"
      >
        <template v-if="showPopular">
          <el-option-group label="热门省份">
            <el-option
              v-for="province in popularProvinces"
              :key="province.value"
              :label="province.label"
              :value="province.value"
            />
          </el-option-group>
        </template>
        <el-option-group label="全部省份">
          <el-option
            v-for="province in filteredProvinces"
            :key="province.value"
            :label="province.label"
            :value="province.value"
          />
        </el-option-group>
      </el-select>
    </el-form-item>

    <!-- 城市选择 -->
    <el-form-item :label="cityLabel" prop="city">
      <el-select
        v-model="selectedCity"
        :placeholder="cityPlaceholder"
        filterable
        :filter-method="filterCities"
        @change="handleCityChange"
        @clear="handleClearCity"
        clearable
        :disabled="disabled || !selectedProvince"
        :loading="cityLoading"
      >
        <el-option
          v-for="city in filteredCities"
          :key="city.value"
          :label="city.label"
          :value="city.value"
        />
      </el-select>
    </el-form-item>

    <!-- 区县选择 -->
    <el-form-item :label="districtLabel" prop="district">
      <el-select
        v-model="selectedDistrict"
        :placeholder="districtPlaceholder"
        filterable
        :filter-method="filterDistricts"
        @change="handleDistrictChange"
        @clear="handleClearDistrict"
        clearable
        :disabled="disabled || !selectedCity"
        :loading="districtLoading"
      >
        <el-option
          v-for="district in filteredDistricts"
          :key="district.value"
          :label="district.label"
          :value="district.value"
        />
      </el-select>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { provinces, citiesData, districtsData, type RegionOption } from '@/data/regionDataFull'
import {
  getCitiesByProvince,
  getDistrictsByCity,
  searchRegions,
  getPopularProvinces
} from '@/utils/regionUtils'

// Props
interface Props {
  modelValue?: {
    province?: string
    city?: string
    district?: string
  }
  provinceLabel?: string
  cityLabel?: string
  districtLabel?: string
  provincePlaceholder?: string
  cityPlaceholder?: string
  districtPlaceholder?: string
  showPopular?: boolean
  disabled?: boolean
  autoSelect?: boolean // 是否自动选择第一个选项
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
  provinceLabel: '省份',
  cityLabel: '城市',
  districtLabel: '区县',
  provincePlaceholder: '请选择省份',
  cityPlaceholder: '请选择城市',
  districtPlaceholder: '请选择区县',
  showPopular: true,
  disabled: false,
  autoSelect: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: { province?: string; city?: string; district?: string }]
  'change': [value: { province?: string; city?: string; district?: string }]
}>()

// 响应式数据
const selectedProvince = ref<string>('')
const selectedCity = ref<string>('')
const selectedDistrict = ref<string>('')
const cityLoading = ref(false)
const districtLoading = ref(false)
const cities = ref<RegionOption[]>([])
const districts = ref<RegionOption[]>([])
const filteredProvinces = ref<RegionOption[]>([])
const filteredCities = ref<RegionOption[]>([])
const filteredDistricts = ref<RegionOption[]>([])
const popularProvinces = ref<RegionOption[]>([])

// 计算属性
const currentValue = computed(() => ({
  province: selectedProvince.value || undefined,
  city: selectedCity.value || undefined,
  district: selectedDistrict.value || undefined
}))

// 方法
// 省份变化处理
const handleProvinceChange = async (province: string) => {
  selectedCity.value = ''
  selectedDistrict.value = ''

  if (province) {
    cityLoading.value = true
    try {
      // 模拟异步加载
      await new Promise(resolve => setTimeout(resolve, 100))
      cities.value = getCitiesByProvince(province)
      filteredCities.value = [...cities.value]

      if (props.autoSelect && cities.value.length > 0) {
        selectedCity.value = cities.value[0].value
        await handleCityChange(selectedCity.value)
      }
    } catch (error) {
      ElMessage.error('加载城市数据失败')
    } finally {
      cityLoading.value = false
    }
  } else {
    cities.value = []
    districts.value = []
    filteredCities.value = []
    filteredDistricts.value = []
  }

  emitChange()
}

// 城市变化处理
const handleCityChange = async (city: string) => {
  selectedDistrict.value = ''

  if (city) {
    districtLoading.value = true
    try {
      // 模拟异步加载
      await new Promise(resolve => setTimeout(resolve, 100))
      districts.value = getDistrictsByCity(city)
      filteredDistricts.value = [...districts.value]

      if (props.autoSelect && districts.value.length > 0) {
        selectedDistrict.value = districts.value[0].value
      }
    } catch (error) {
      ElMessage.error('加载区县数据失败')
    } finally {
      districtLoading.value = false
    }
  } else {
    districts.value = []
    filteredDistricts.value = []
  }

  emitChange()
}

// 区县变化处理
const handleDistrictChange = () => {
  emitChange()
}

// 清除省份
const handleClearProvince = () => {
  selectedProvince.value = ''
  selectedCity.value = ''
  selectedDistrict.value = ''
  cities.value = []
  districts.value = []
  filteredCities.value = []
  filteredDistricts.value = []
  emitChange()
}

// 清除城市
const handleClearCity = () => {
  selectedCity.value = ''
  selectedDistrict.value = ''
  districts.value = []
  filteredDistricts.value = []
  emitChange()
}

// 清除区县
const handleClearDistrict = () => {
  selectedDistrict.value = ''
  emitChange()
}

// 触发变化事件
const emitChange = () => {
  const value = currentValue.value
  emit('update:modelValue', value)
  emit('change', value)
}

// 省份过滤
const filterProvinces = (query: string) => {
  if (query) {
    filteredProvinces.value = searchRegions(query, 'province')
  } else {
    filteredProvinces.value = [...provinces]
  }
}

// 城市过滤
const filterCities = (query: string) => {
  if (query) {
    const allCities: RegionOption[] = []
    cities.value.forEach(city => {
      allCities.push(city)
    })
    filteredCities.value = allCities.filter(city =>
      city.label.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    filteredCities.value = [...cities.value]
  }
}

// 区县过滤
const filterDistricts = (query: string) => {
  if (query) {
    const allDistricts: RegionOption[] = []
    districts.value.forEach(district => {
      allDistricts.push(district)
    })
    filteredDistricts.value = allDistricts.filter(district =>
      district.label.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    filteredDistricts.value = [...districts.value]
  }
}

// 初始化数据
const initializeData = () => {
  filteredProvinces.value = [...provinces]
  popularProvinces.value = getPopularProvinces()
}

// 设置值
const setValue = (value: { province?: string; city?: string; district?: string }) => {
  if (value.province !== selectedProvince.value) {
    selectedProvince.value = value.province || ''
    handleProvinceChange(selectedProvince.value)
  }

  if (value.city !== selectedCity.value) {
    selectedCity.value = value.city || ''
    handleCityChange(selectedCity.value)
  }

  if (value.district !== selectedDistrict.value) {
    selectedDistrict.value = value.district || ''
  }
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    setValue(newValue)
  }
}, { immediate: true, deep: true })

// 组件挂载
onMounted(() => {
  initializeData()
})

// 暴露方法
defineExpose({
  setValue,
  reset: () => setValue({}),
  getValue: () => currentValue.value
})
</script>

<style scoped>
.region-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.region-selector :deep(.el-form-item) {
  margin-bottom: 0;
}

.region-selector :deep(.el-select) {
  width: 100%;
}

/* 响应式布局 */
@media (min-width: 768px) {
  .region-selector {
    flex-direction: row;
    align-items: flex-start;
    gap: 16px;
  }

  .region-selector :deep(.el-form-item) {
    flex: 1;
    margin-bottom: 0;
  }
}
</style>