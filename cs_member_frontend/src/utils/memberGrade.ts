import request from '@/utils/request'
import type { MemberGradeConfig } from '@/types'

/**
 * 会员等级工具类
 * 用于计算会员等级、等级颜色等相关功能
 */
export class MemberGradeUtil {
  private static gradeConfigs: MemberGradeConfig[] = []
  private static loaded = false

  /**
   * 获取等级配置列表
   */
  static async getGradeConfigs(): Promise<MemberGradeConfig[]> {
    if (this.loaded && this.gradeConfigs.length > 0) {
      return this.gradeConfigs
    }

    try {
      const response = await request.get<MemberGradeConfig[]>('/member/grade-config/list')
      this.gradeConfigs = response.data || []
      this.loaded = true
      return this.gradeConfigs
    } catch (error) {
      // 返回默认等级配置
      this.gradeConfigs = this.getDefaultGradeConfigs()
      this.loaded = true
      return this.gradeConfigs
    }
  }

  /**
   * 使用等级配置表计算等级（主要方法）
   * @param points 积分
   * @param gradeConfigs 等级配置表
   * @returns 匹配的等级配置
   */
  static calculateGradeFromConfigs(points: number, gradeConfigs: MemberGradeConfig[]): MemberGradeConfig | null {
    if (!gradeConfigs || gradeConfigs.length === 0) {
      return null
    }

    // 按积分范围从高到低查找匹配的等级
    for (let i = gradeConfigs.length - 1; i >= 0; i--) {
      const config = gradeConfigs[i]
      if (points >= config.minPoints) {
        // 如果有maxPoints限制，需要检查是否在范围内
        if (config.maxPoints !== null && config.maxPoints !== undefined && points > config.maxPoints) {
          continue
        }
        return config
      }
    }

    // 如果没有找到匹配的等级，返回最低等级
    return gradeConfigs[0]
  }

  /**
   * 获取默认等级配置（备用）
   */
  private static getDefaultGradeConfigs(): MemberGradeConfig[] {
    return [
      { id: 7, gradeCode: 'VIP1', gradeName: 'VIP1会员', minPoints: 0, maxPoints: 999, pointsMultiplier: 1.00, discountRate: 99.00, color: '#C0C0C0', sortOrder: 1, status: 1 },
      { id: 8, gradeCode: 'VIP2', gradeName: 'VIP2会员', minPoints: 1000, maxPoints: 2999, pointsMultiplier: 1.10, discountRate: 98.00, color: '#87CEEB', sortOrder: 2, status: 1 },
      { id: 9, gradeCode: 'VIP3', gradeName: 'VIP3会员', minPoints: 3000, maxPoints: 5999, pointsMultiplier: 1.20, discountRate: 95.00, color: '#FFD700', sortOrder: 3, status: 1 },
      { id: 10, gradeCode: 'VIP4', gradeName: 'VIP4会员', minPoints: 6000, maxPoints: 9999, pointsMultiplier: 1.30, discountRate: 92.00, color: '#FF8C00', sortOrder: 4, status: 1 },
      { id: 11, gradeCode: 'VIP5', gradeName: 'VIP5会员', minPoints: 10000, maxPoints: 19999, pointsMultiplier: 1.50, discountRate: 88.00, color: '#FF6347', sortOrder: 5, status: 1 },
      { id: 12, gradeCode: 'VIP6', gradeName: 'VIP6会员', minPoints: 20000, maxPoints: 49999, pointsMultiplier: 1.80, discountRate: 85.00, color: '#9370DB', sortOrder: 6, status: 1 },
      { id: 13, gradeCode: 'VIP7', gradeName: 'VIP7会员', minPoints: 50000, maxPoints: null, pointsMultiplier: 2.00, discountRate: 77.00, color: '#FF1493', sortOrder: 7, status: 1 }
    ]
  }

  /**
   * 根据积分计算会员等级
   * @param points 累计积分
   * @returns 等级信息
   */
  static async calculateGrade(points: number): Promise<MemberGradeConfig | null> {
    const configs = await this.getGradeConfigs()

    // 按积分范围从高到低查找匹配的等级
    for (let i = configs.length - 1; i >= 0; i--) {
      const config = configs[i]
      if (points >= config.minPoints) {
        // 如果有maxPoints限制，需要检查是否在范围内
        if (config.maxPoints !== null && config.maxPoints !== undefined && points > config.maxPoints) {
          continue
        }
        return config
      }
    }

    // 如果没有找到匹配的等级，返回最低等级
    return configs.length > 0 ? configs[0] : null
  }

  /**
   * 根据积分同步计算会员等级（不等待异步加载）
   * @param points 累计积分
   * @returns 等级信息（可能为空如果配置还未加载）
   */
  static calculateGradeSync(points: number): MemberGradeConfig | null {
    if (!this.loaded || this.gradeConfigs.length === 0) {
      return null
    }

    // 按积分范围从高到低查找匹配的等级
    for (let i = this.gradeConfigs.length - 1; i >= 0; i--) {
      const config = this.gradeConfigs[i]
      if (points >= config.minPoints) {
        // 如果有maxPoints限制，需要检查是否在范围内
        if (config.maxPoints !== null && config.maxPoints !== undefined && points > config.maxPoints) {
          continue
        }
        return config
      }
    }

    // 如果没有找到匹配的等级，返回最低等级
    return this.gradeConfigs.length > 0 ? this.gradeConfigs[0] : null
  }

  /**
   * 获取等级样式类名
   * @param gradeCode 等级编码
   * @returns CSS类名
   */
  static getGradeClass(gradeCode: string): string {
    return `grade-${gradeCode.toLowerCase()}`
  }

  /**
   * 获取等级进度百分比
   * @param currentPoints 当前积分
   * @param currentGrade 当前等级
   * @param nextGrade 下一等级
   * @returns 进度百分比 (0-100)
   */
  static getGradeProgress(currentPoints: number, currentGrade: MemberGradeConfig, nextGrade: MemberGradeConfig | null): number {
    if (!nextGrade) {
      return 100 // 已经是最高等级
    }

    const currentMin = currentGrade.minPoints
    const nextMin = nextGrade.minPoints
    const range = nextMin - currentMin

    if (range <= 0) {
      return 100
    }

    const progress = ((currentPoints - currentMin) / range) * 100
    return Math.min(Math.max(progress, 0), 100)
  }

  /**
   * 获取等级升级所需积分
   * @param currentPoints 当前积分
   * @param currentGrade 当前等级
   * @param nextGrade 下一等级
   * @returns 升级所需积分，如果已是最高等级返回0
   */
  static getPointsToNextLevel(currentPoints: number, currentGrade: MemberGradeConfig, nextGrade: MemberGradeConfig | null): number {
    if (!nextGrade) {
      return 0 // 已经是最高等级
    }

    const needed = nextGrade.minPoints - currentPoints
    return Math.max(needed, 0)
  }

  /**
   * 预加载等级配置
   * 建议在应用启动时调用
   */
  static async preloadGradeConfigs(): Promise<void> {
    await this.getGradeConfigs()
  }

  /**
   * 清除缓存的等级配置
   */
  static clearCache(): void {
    this.gradeConfigs = []
    this.loaded = false
  }

  /**
   * 将MemberGradeInfo转换为MemberGradeConfig
   * @param gradeInfo MemberGradeInfo对象
   * @returns MemberGradeConfig对象
   */
  static convertToMemberGradeConfig(gradeInfo: any): MemberGradeConfig {
    return {
      id: 0, // MemberGradeInfo没有id字段
      gradeCode: gradeInfo.gradeCode,
      gradeName: gradeInfo.gradeName,
      minPoints: 0, // MemberGradeInfo没有minPoints字段
      maxPoints: null, // MemberGradeInfo没有maxPoints字段
      pointsMultiplier: gradeInfo.pointsMultiplier / 100, // 转换为小数
      discountRate: gradeInfo.discountRate / 100, // 转换为小数
      color: gradeInfo.color,
      sortOrder: 0, // MemberGradeInfo没有sortOrder字段
      status: 1 // 默认启用状态
    }
  }
}

/**
 * 便捷方法：根据积分获取等级信息
 * @param points 积分
 * @returns Promise<MemberGradeConfig | null>
 */
export async function getMemberGrade(points: number): Promise<MemberGradeConfig | null> {
  return MemberGradeUtil.calculateGrade(points)
}

/**
 * 便捷方法：获取等级样式
 * @param gradeCode 等级编码
 * @returns CSS类名
 */
export function getGradeClass(gradeCode: string): string {
  return MemberGradeUtil.getGradeClass(gradeCode)
}

/**
 * 便捷方法：获取等级进度
 * @param currentPoints 当前积分
 * @param currentGrade 当前等级
 * @param nextGrade 下一等级
 * @returns 进度百分比
 */
export function getGradeProgress(currentPoints: number, currentGrade: MemberGradeConfig, nextGrade: MemberGradeConfig | null): number {
  return MemberGradeUtil.getGradeProgress(currentPoints, currentGrade, nextGrade)
}