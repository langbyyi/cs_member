/**
 * 导出功能工具函数
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */

import * as XLSX from 'xlsx'
import dayjs from 'dayjs'

/**
 * 导出员工数据为Excel文件
 * @param data 员工数据数组
 * @param filename 文件名(不含扩展名)
 */
export function exportEmployeesToExcel(data: any[], filename: string = '员工列表') {
  if (!data || data.length === 0) {
    return
  }

  // 转换数据格式
  const exportData = data.map(employee => ({
    '工号': employee.employeeNo || '-',
    '姓名': employee.name,
    '手机号': employee.phone,
    '邮箱': employee.email || '-',
    '性别': employee.gender === 1 ? '男' : employee.gender === 2 ? '女' : '未知',
    '所属门店': employee.storeName || '-',
    '职位': employee.roleName,
    '状态': employee.status === 1 ? '正常' : employee.status === 0 ? '已停用' : '已锁定',
    '登录次数': employee.loginCount || 0,
    '最后登录时间': employee.lastLoginTime ? dayjs(employee.lastLoginTime).format('YYYY-MM-DD HH:mm:ss') : '从未登录',
    '入职时间': dayjs(employee.createdTime).format('YYYY-MM-DD HH:mm:ss')
  }))

  // 创建工作簿
  const worksheet = XLSX.utils.json_to_sheet(exportData)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '员工列表')

  // 设置列宽
  const colWidths = [
    { wch: 15 }, // 工号
    { wch: 12 }, // 姓名
    { wch: 15 }, // 手机号
    { wch: 25 }, // 邮箱
    { wch: 8 },  // 性别
    { wch: 20 }, // 所属门店
    { wch: 10 }, // 职位
    { wch: 10 }, // 状态
    { wch: 10 }, // 登录次数
    { wch: 20 }, // 最后登录时间
    { wch: 20 }  // 入职时间
  ]
  worksheet['!cols'] = colWidths

  // 生成文件名
  const timestamp = dayjs().format('YYYYMMDDHHmmss')
  const finalFilename = `${filename}_${timestamp}.xlsx`

  // 导出文件
  XLSX.writeFile(workbook, finalFilename)
}

/**
 * 导出会员数据为Excel文件
 * @param data 会员数据数组
 * @param filename 文件名(不含扩展名)
 */
export function exportMembersToExcel(data: any[], filename: string = '会员列表') {
  if (!data || data.length === 0) {
    return
  }

  // 转换数据格式
  const exportData = data.map(member => ({
    '会员编号': member.memberNo,
    '姓名': member.name,
    '手机号': member.phone,
    '性别': member.gender === 1 ? '男' : member.gender === 2 ? '女' : '未知',
    '会员等级': member.memberLevelName || '-',
    '当前积分': member.currentPoints || 0,
    '累计积分': member.totalPoints || 0,
    '账户余额': member.balance || 0,
    '累计消费': member.totalConsumption || 0,
    '消费次数': member.consumptionCount || 0,
    '注册门店': member.registerStoreName || '-',
    '状态': member.status === 1 ? '正常' : '已停用',
    '注册时间': dayjs(member.createdTime).format('YYYY-MM-DD HH:mm:ss'),
    '最后消费时间': member.lastConsumptionTime ? dayjs(member.lastConsumptionTime).format('YYYY-MM-DD HH:mm:ss') : '从未消费'
  }))

  // 创建工作簿
  const worksheet = XLSX.utils.json_to_sheet(exportData)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '会员列表')

  // 设置列宽
  const colWidths = [
    { wch: 15 }, // 会员编号
    { wch: 12 }, // 姓名
    { wch: 15 }, // 手机号
    { wch: 8 },  // 性别
    { wch: 12 }, // 会员等级
    { wch: 12 }, // 当前积分
    { wch: 12 }, // 累计积分
    { wch: 12 }, // 账户余额
    { wch: 12 }, // 累计消费
    { wch: 10 }, // 消费次数
    { wch: 20 }, // 注册门店
    { wch: 10 }, // 状态
    { wch: 20 }, // 注册时间
    { wch: 20 }  // 最后消费时间
  ]
  worksheet['!cols'] = colWidths

  // 生成文件名
  const timestamp = dayjs().format('YYYYMMDDHHmmss')
  const finalFilename = `${filename}_${timestamp}.xlsx`

  // 导出文件
  XLSX.writeFile(workbook, finalFilename)
}

/**
 * 导出消费记录为Excel文件
 * @param data 消费记录数据数组
 * @param filename 文件名(不含扩展名)
 */
export function exportConsumptionToExcel(data: any[], filename: string = '消费记录') {
  if (!data || data.length === 0) {
    return
  }

  // 转换数据格式
  const exportData = data.map(record => ({
    '消费单号': record.consumptionNo,
    '会员姓名': record.memberName,
    '会员手机': record.memberPhone,
    '门店名称': record.storeName,
    '消费金额': record.amount,
    '优惠金额': record.discountAmount || 0,
    '实付金额': record.actualAmount,
    '获得积分': record.pointsEarned || 0,
    '使用积分': record.pointsUsed || 0,
    '使用优惠券': record.couponName || '-',
    '支付方式': record.paymentMethodName,
    '操作员': record.operatorName,
    '消费时间': dayjs(record.consumptionTime).format('YYYY-MM-DD HH:mm:ss'),
    '备注': record.remark || '-'
  }))

  // 创建工作簿
  const worksheet = XLSX.utils.json_to_sheet(exportData)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '消费记录')

  // 设置列宽
  const colWidths = [
    { wch: 20 }, // 消费单号
    { wch: 12 }, // 会员姓名
    { wch: 15 }, // 会员手机
    { wch: 20 }, // 门店名称
    { wch: 12 }, // 消费金额
    { wch: 12 }, // 优惠金额
    { wch: 12 }, // 实付金额
    { wch: 10 }, // 获得积分
    { wch: 10 }, // 使用积分
    { wch: 20 }, // 使用优惠券
    { wch: 12 }, // 支付方式
    { wch: 12 }, // 操作员
    { wch: 20 }, // 消费时间
    { wch: 30 }  // 备注
  ]
  worksheet['!cols'] = colWidths

  // 生成文件名
  const timestamp = dayjs().format('YYYYMMDDHHmmss')
  const finalFilename = `${filename}_${timestamp}.xlsx`

  // 导出文件
  XLSX.writeFile(workbook, finalFilename)
}

/**
 * 导出门店数据为Excel文件
 * @param data 门店数据数组
 * @param filename 文件名(不含扩展名)
 */
export function exportStoresToExcel(data: any[], filename: string = '门店列表') {
  if (!data || data.length === 0) {
    return
  }

  // 转换数据格式
  const exportData = data.map(store => ({
    '门店编号': store.storeNo,
    '门店名称': store.storeName,
    '门店类型': store.storeType,
    '省份': store.province,
    '城市': store.city,
    '区县': store.district,
    '详细地址': store.address,
    '联系电话': store.contactPhone,
    '员工数量': store.employeeCount || 0,
    '会员数量': store.memberCount || 0,
    '状态': store.status === 1 ? '营业中' : '已关闭',
    '创建时间': store.createTime ? dayjs(store.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'
  }))

  // 创建工作簿
  const worksheet = XLSX.utils.json_to_sheet(exportData)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '门店列表')

  // 设置列宽
  const colWidths = [
    { wch: 15 }, // 门店编号
    { wch: 20 }, // 门店名称
    { wch: 12 }, // 门店类型
    { wch: 10 }, // 省份
    { wch: 10 }, // 城市
    { wch: 10 }, // 区县
    { wch: 30 }, // 详细地址
    { wch: 15 }, // 联系电话
    { wch: 10 }, // 员工数量
    { wch: 10 }, // 会员数量
    { wch: 10 }, // 状态
    { wch: 20 }  // 创建时间
  ]
  worksheet['!cols'] = colWidths

  // 生成文件名
  const timestamp = dayjs().format('YYYYMMDDHHmmss')
  const finalFilename = `${filename}_${timestamp}.xlsx`

  // 导出文件
  XLSX.writeFile(workbook, finalFilename)
}

/**
 * 导出到Excel (通用方法,保留向后兼容)
 * @param data 数据
 * @param filename 文件名
 */
export function exportToExcel(data: any[], filename: string) {
  // 简单的CSV导出实现
  if (!data || data.length === 0) {
    return
  }

  // 获取表头
  const headers = Object.keys(data[0])

  // 构建CSV内容
  const csvContent = [
    headers.join(','),
    ...data.map(row =>
      headers.map(header => {
        const value = row[header]
        // 处理包含逗号或引号的值
        if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
          return `"${value.replace(/"/g, '""')}"`
        }
        return value || ''
      }).join(',')
    )
  ].join('\n')

  // 创建Blob对象
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })

  // 创建下载链接
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${filename}.csv`)
  link.style.visibility = 'hidden'

  // 触发下载
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 导出为JSON文件
 * @param data 数据
 * @param filename 文件名
 */
export function exportToJson(data: any, filename: string) {
  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: 'application/json' })

  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${filename}.json`)
  link.style.visibility = 'hidden'

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}