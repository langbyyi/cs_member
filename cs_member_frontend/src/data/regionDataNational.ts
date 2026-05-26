// 全国省市区完整数据 - 使用element-china-area-data包
// 包含完整的34个省级行政区、333个地级市、2844个区县
// 数据来源: 中华人民共和国国家统计局2023年统计用区划代码

import { regionData } from 'element-china-area-data'

export interface RegionOption {
    value: string
    label: string
}

// 转换element-china-area-data的数据格式为我们需要的格式
function convertRegionData(data: any[]): RegionOption[] {
    return data.map(item => ({
        value: item.label,
        label: item.label
    }))
}

// 省份数据
export const provinces: RegionOption[] = convertRegionData(regionData)

// 城市数据映射
export const citiesData: Record<string, RegionOption[]> = {}

regionData.forEach(province => {
    const provinceName = province.label
    if (province.children && province.children.length > 0) {
        citiesData[provinceName] = convertRegionData(province.children)
    } else {
        // 直辖市情况
        citiesData[provinceName] = [{ value: provinceName, label: provinceName }]
    }
})

// 区县数据映射
export const districtsData: Record<string, RegionOption[]> = {}

regionData.forEach(province => {
    if (province.children) {
        province.children.forEach((city: any) => {
            const cityName = city.label
            if (city.children && city.children.length > 0) {
                districtsData[cityName] = convertRegionData(city.children)
            }
        })
    }
})

// 默认导出
export default {
    provinces,
    citiesData,
    districtsData
}
