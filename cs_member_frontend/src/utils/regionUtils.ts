import { citiesData, districtsData, type RegionOption } from '@/data/regionDataFull'

// 获取省份对应的城市列表
export function getCitiesByProvince(province: string): RegionOption[] {
  return citiesData[province] || []
}

// 获取城市对应的区县列表
export function getDistrictsByCity(city: string): RegionOption[] {
  return districtsData[city] || []
}

// 格式化省市区完整地址
export function formatFullAddress(province: string, city: string, district: string, address: string): string {
  const parts = [province, city, district, address].filter(Boolean)
  return parts.join('')
}

// 验证省市区选择的完整性
export function validateRegionSelection(province: string, city: string, district: string): boolean {
  return !!(province && city && district)
}

// 模糊搜索地区（支持省市区三级搜索）
export function searchRegions(keyword: string, type: 'province' | 'city' | 'district' = 'province'): RegionOption[] {
  if (!keyword) return []

  const results: RegionOption[] = []
  const lowerKeyword = keyword.toLowerCase()

  if (type === 'province') {
    // 搜索省份
    Object.keys(citiesData).forEach(province => {
      if (province.toLowerCase().includes(lowerKeyword)) {
        results.push({ value: province, label: province })
      }
    })
  } else if (type === 'city') {
    // 搜索城市
    Object.entries(citiesData).forEach(([province, cities]) => {
      cities.forEach(city => {
        if (city.label.toLowerCase().includes(lowerKeyword)) {
          results.push({
            value: city.value,
            label: `${city.label} (${province})`
          })
        }
      })
    })
  } else if (type === 'district') {
    // 搜索区县
    Object.entries(districtsData).forEach(([city, districts]) => {
      districts.forEach(district => {
        if (district.label.toLowerCase().includes(lowerKeyword)) {
          results.push({
            value: district.value,
            label: `${district.label} (${city})`
          })
        }
      })
    })
  }

  return results.slice(0, 20) // 限制搜索结果数量
}

// 获取热门省份
export function getPopularProvinces(): RegionOption[] {
  return [
    { value: '北京市', label: '北京市' },
    { value: '上海市', label: '上海市' },
    { value: '广东省', label: '广东省' },
    { value: '江苏省', label: '江苏省' },
    { value: '浙江省', label: '浙江省' },
    { value: '山东省', label: '山东省' },
    { value: '河南省', label: '河南省' },
    { value: '四川省', label: '四川省' },
    { value: '湖北省', label: '湖北省' },
    { value: '湖南省', label: '湖南省' }
  ]
}

// 获取省份的所有城市名称（用于验证）
export function getProvinceCitiesNames(province: string): string[] {
  return getCitiesByProvince(province).map(city => city.label)
}

// 获取城市的所有区县名称（用于验证）
export function getCityDistrictsNames(city: string): string[] {
  return getDistrictsByCity(city).map(district => district.label)
}