/**
 * Excel导出工具函数
 */

interface ExcelHeader {
  title: string
  key: string
}

/**
 * 导出数据到Excel文件
 * @param data 要导出的数据数组
 * @param filename 文件名（不包含扩展名）
 * @param headers 表头配置
 */
export const exportToExcel = (data: any[], filename: string, headers: ExcelHeader[]) => {
  try {
    // 创建CSV内容
    const csvContent = generateCSV(data, headers)
    
    // 创建Blob对象
    const blob = new Blob(['\uFEFF' + csvContent], { 
      type: 'text/csv;charset=utf-8;' 
    })
    
    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `${filename}.csv`)
    link.style.visibility = 'hidden'
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 释放URL对象
    URL.revokeObjectURL(url)
    
    // 显示成功消息
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.success('导出成功')
    })
  } catch (error) {
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.error('导出失败，请重试')
    })
  }
}

/**
 * 生成CSV内容
 * @param data 数据数组
 * @param headers 表头配置
 * @returns CSV字符串
 */
const generateCSV = (data: any[], headers: ExcelHeader[]): string => {
  // 生成表头行
  const headerRow = headers.map(header => `"${header.title}"`).join(',')
  
  // 生成数据行
  const dataRows = data.map(row => {
    return headers.map(header => {
      const value = row[header.key]
      // 处理特殊字符和空值
      const cellValue = value === null || value === undefined ? '' : String(value)
      // 转义双引号并用双引号包裹
      return `"${cellValue.replace(/"/g, '""')}"`
    }).join(',')
  })
  
  // 组合表头和数据
  return [headerRow, ...dataRows].join('\n')
}

/**
 * 导出JSON数据（备用方法）
 * @param data 要导出的数据
 * @param filename 文件名
 */
export const exportToJSON = (data: any, filename: string) => {
  try {
    const jsonString = JSON.stringify(data, null, 2)
    const blob = new Blob([jsonString], { 
      type: 'application/json;charset=utf-8;' 
    })
    
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `${filename}.json`)
    link.style.visibility = 'hidden'
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    URL.revokeObjectURL(url)
    
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.success('导出成功')
    })
  } catch (error) {
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.error('导出失败，请重试')
    })
  }
}