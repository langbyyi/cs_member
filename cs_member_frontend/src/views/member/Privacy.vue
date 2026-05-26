<template>
  <div class="privacy-page">
    <div class="page-header">
      <div class="header-content">
        <el-button
          type="text"
          @click="goBack"
          class="back-btn"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>隐私政策</h1>
        <p class="page-subtitle">我们重视您的隐私保护，请仔细阅读以下隐私政策</p>
        <div class="meta-info">
          <span class="update-date">最后更新：2025年1月18日</span>
          <span class="version">版本：V1.0</span>
        </div>
      </div>
    </div>

    <div class="content-container">
      <div class="privacy-content">
        <div class="section" v-for="(section, index) in sections" :key="index" :id="`section-${index + 1}`">
          <h2 class="section-title">
            <span class="section-number">{{ index + 1 }}</span>
            {{ section.title }}
          </h2>

          <div class="section-content">
            <p v-for="(paragraph, pIndex) in section.content" :key="pIndex" v-html="paragraph"></p>

            <div v-if="section.subsections" class="subsections">
              <div v-for="(subsection, subIndex) in section.subsections" :key="subIndex" class="subsection">
                <h3 class="subsection-title">
                  <span class="subsection-number">{{ index + 1 }}.{{ subIndex + 1 }}</span>
                  {{ subsection.title }}
                </h3>
                <div class="subsection-content">
                  <p v-for="(paragraph, pIndex) in subsection.content" :key="pIndex" v-html="paragraph"></p>

                  <ul v-if="subsection.list" class="privacy-list">
                    <li v-for="(item, listIndex) in subsection.list" :key="listIndex" v-html="item"></li>
                  </ul>
                </div>
              </div>
            </div>

            <ul v-if="section.list" class="privacy-list">
              <li v-for="(item, listIndex) in section.list" :key="listIndex" v-html="item"></li>
            </ul>
          </div>
        </div>
      </div>

      <div class="table-of-contents" :class="{ 'sticky': isSticky }">
        <h3>目录导航</h3>
        <ul class="toc-list">
          <li v-for="(section, index) in sections" :key="index" class="toc-item">
            <a :href="`#section-${index + 1}`" @click.prevent="scrollToSection(index + 1)">
              <span class="toc-number">{{ index + 1 }}</span>
              {{ section.title }}
            </a>
          </li>
        </ul>
      </div>
    </div>

    <div class="page-footer">
      <div class="footer-content">
        <p>我们承诺保护您的个人信息安全，严格按照本政策处理您的数据</p>
        <div class="footer-actions">
          <el-button @click="printPage">
            <el-icon><Printer /></el-icon>
            打印政策
          </el-button>
          <el-button type="primary" @click="goBack">
            我已阅读完成
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Printer } from '@element-plus/icons-vue'

interface Subsection {
  title: string
  content: string[]
  list?: string[]
}

interface Section {
  title: string
  content: string[]
  subsections?: Subsection[]
  list?: string[]
}

const router = useRouter()
const isSticky = ref(false)

// 隐私政策内容
const sections: Section[] = [
  {
    title: '信息收集',
    content: [
      '为了向用户提供优质的会员服务，我们需要收集和使用一些个人信息。我们承诺会严格遵守相关法律法规，保护您的隐私安全。'
    ],
    subsections: [
      {
        title: '主动提供的信息',
        content: [
          '当您注册使用本系统时，我们会收集您主动提供的信息，包括：'
        ],
        list: [
          '<strong>基本信息：</strong>真实姓名、性别、出生日期等',
          '<strong>联系方式：</strong>手机号码、电子邮箱地址等',
          '<strong>账号信息：</strong>用户名、密码、安全问题等',
          '<strong>偏好设置：</strong>语言偏好、消息通知偏好等'
        ]
      },
      {
        title: '自动收集的信息',
        content: [
          '在您使用本系统服务过程中，我们会自动收集一些技术信息，包括：'
        ],
        list: [
          '<strong>设备信息：</strong>设备型号、操作系统、浏览器信息等',
          '<strong>位置信息：</strong>IP地址、大致地理位置（基于IP地址）',
          '<strong>使用信息：</strong>访问时间、页面停留时间、操作记录等',
          '<strong>日志信息：</strong>系统日志、错误日志、安全日志等'
        ]
      }
    ]
  },
  {
    title: '信息使用',
    content: [
      '我们使用收集的信息来提供、维护和改进我们的服务：'
    ],
    list: [
      '<strong>服务提供：</strong>创建账号、身份验证、功能使用等',
      '<strong>客户支持：</strong>问题解答、技术支持、投诉处理等',
      '<strong>安全保护：</strong>身份验证、风险评估、欺诈检测等',
      '<strong>系统优化：</strong>性能分析、用户体验改进、功能开发等',
      '<strong>营销推广：</strong>个性化推荐、优惠信息推送（需获得用户同意）',
      '<strong>合规要求：</strong>满足法律法规要求、监管报告等'
    ]
  },
  {
    title: '信息共享',
    content: [
      '除以下情况外，我们不会向第三方分享您的个人信息：'
    ],
    list: [
      '<strong>用户同意：</strong>在获得用户明确同意的情况下分享信息',
      '<strong>法律要求：</strong>根据法律法规、政府要求或司法命令必须分享',
      '<strong>服务提供：</strong>为提供服务需要，与合作伙伴共享必要信息（已签署保密协议）',
      '<strong>业务转让：</strong>在合并、收购、资产转让等情况下，与接收方共享必要信息',
      '<strong>紧急情况：</strong>为保护用户或公众的人身、财产安全而必须分享'
    ]
  },
  {
    title: '信息存储',
    content: [
      '我们采用业界标准的安全措施来保护您的信息安全：'
    ],
    list: [
      '<strong>数据加密：</strong>敏感数据采用加密存储和传输',
      '<strong>访问控制：</strong>严格的权限管理和访问控制机制',
      '<strong>安全审计：</strong>定期进行安全检查和漏洞扫描',
      '<strong>数据备份：</strong>定期备份重要数据，防止数据丢失',
      '<strong>员工培训：</strong>对接触用户信息的员工进行隐私保护培训'
    ]
  },
  {
    title: '用户权利',
    content: [
      '根据相关法律法规，您对个人信息享有以下权利：'
    ],
    subsections: [
      {
        title: '知情权',
        content: [
          '您有权了解我们收集、使用、存储您个人信息的目的、方式和范围。'
        ]
      },
      {
        title: '访问权',
        content: [
          '您有权访问和获取我们持有的您的个人信息副本。'
        ]
      },
      {
        title: '更正权',
        content: [
          '当您发现个人信息不准确时，有权要求我们进行更正。'
        ]
      },
      {
        title: '删除权',
        content: [
          '在符合法律法规规定的情况下，您有权要求我们删除您的个人信息。'
        ]
      },
      {
        title: '投诉举报',
        content: [
          '如您认为我们违反了隐私保护相关法律法规，有权向监管部门投诉举报。'
        ]
      }
    ]
  },
  {
    title: 'Cookie使用',
    content: [
      '为了提供更好的用户体验，我们的系统可能使用Cookie和类似技术：'
    ],
    list: [
      '<strong>必要Cookie：</strong>确保系统基本功能正常运行',
      '<strong>性能Cookie：</strong>收集网站使用情况统计信息',
      '<strong>功能Cookie：</strong>记住用户偏好设置和登录状态',
      '<strong>营销Cookie：</strong>用于个性化推荐和广告投放（需用户同意）'
    ]
  },
  {
    title: '数据保留',
    content: [
      '我们仅在必要期限内保留您的个人信息：'
    ],
    list: [
      '<strong>注册期间：</strong>用户账号有效期间持续保留',
      '<strong>法律要求：</strong>根据法律法规要求的最低保存期限',
      '<strong>业务需要：</strong>为解决争议、执行协议等必要的保存期限',
      '<strong>用户同意：</strong>获得用户同意的特定保存期限'
    ],
    subsections: [
      {
        title: '数据删除',
        content: [
          '达到保留期限后，我们将采取删除、匿名化等安全方式处理您的个人信息。'
        ]
      }
    ]
  },
  {
    title: '未成年人保护',
    content: [
      '我们特别重视未成年人的隐私保护。如果您是未成年人，请在监护人指导下使用本系统。',
      '我们不会主动收集未满14周岁儿童的个人信息，如发现收集了此类信息，将立即予以删除。'
    ]
  },
  {
    title: '政策更新',
    content: [
      '我们可能会不时更新本隐私政策。如有重大变更，我们将通过系统公告、邮件通知等方式告知用户。',
      '用户继续使用我们的服务，即表示您同意更新后的隐私政策。建议您定期查看本政策的最新内容。'
    ]
  },
  {
    title: '联系我们',
    content: [
      '如果您对本隐私政策有任何疑问或建议，请通过以下方式联系我们：',
      '<strong>电子邮箱：</strong>privacy@cs-member-system.com',
      '<strong>客服热线：</strong>400-888-8888',
      '<strong>在线客服：</strong>系统内置客服功能',
      '<strong>邮寄地址：</strong>北京市朝阳区XXX大厦XX层'
    ]
  }
]

const goBack = () => {
  window.close()
  // 如果窗口关闭失败，则返回上一页
  setTimeout(() => {
    router.back()
  }, 100)
}

const scrollToSection = (sectionNumber: number) => {
  const element = document.getElementById(`section-${sectionNumber}`)
  if (element) {
    const offset = 80 // 偏移量，避免被顶部遮挡
    const elementPosition = element.offsetTop - offset
    window.scrollTo({
      top: elementPosition,
      behavior: 'smooth'
    })
  }
}

const printPage = () => {
  window.print()
}

const handleScroll = () => {
  const toc = document.querySelector('.table-of-contents')
  if (toc) {
    const rect = toc.getBoundingClientRect()
    isSticky.value = rect.top <= 80
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.privacy-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f2ff 0%, #dde7ff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  padding: 60px 0 40px;
  text-align: center;
  position: relative;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-btn {
  position: absolute;
  left: 20px;
  top: 20px;
  color: white;
  font-size: 14px;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px 0;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 24px 0;
  line-height: 1.6;
}

.meta-info {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 14px;
  opacity: 0.8;
}

.content-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 40px;
  position: relative;
}

.privacy-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.section {
  margin-bottom: 40px;
  scroll-margin-top: 80px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 24px 0;
  padding-bottom: 12px;
  border-bottom: 3px solid #4CAF50;
}

.section-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border-radius: 50%;
  font-size: 18px;
  font-weight: 700;
  margin-right: 16px;
  flex-shrink: 0;
}

.section-content p {
  margin: 0 0 16px 0;
  line-height: 1.8;
  color: #4a5568;
  font-size: 16px;
}

.section-content p:last-child {
  margin-bottom: 0;
}

.subsections {
  margin-top: 24px;
  padding-left: 20px;
}

.subsection {
  margin-bottom: 24px;
  padding-left: 20px;
  border-left: 4px solid #e4e7ed;
}

.subsection:last-child {
  margin-bottom: 0;
}

.subsection-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #4CAF50;
  margin: 0 0 16px 0;
}

.subsection-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #4CAF50;
  color: white;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.subsection-content p {
  margin-bottom: 12px;
  font-size: 15px;
}

.privacy-list {
  margin: 16px 0;
  padding-left: 24px;
}

.privacy-list li {
  margin-bottom: 12px;
  line-height: 1.7;
  color: #4a5568;
  font-size: 15px;
  list-style-type: none;
  position: relative;
  padding-left: 20px;
}

.privacy-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #4CAF50;
  font-weight: bold;
  font-size: 18px;
}

.privacy-list li:last-child {
  margin-bottom: 0;
}

.table-of-contents {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  height: fit-content;
  top: 20px;
  position: sticky;
}

.table-of-contents.sticky {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.table-of-contents h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #e4e7ed;
}

.toc-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.toc-item {
  margin-bottom: 8px;
}

.toc-item a {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  color: #4a5568;
  text-decoration: none;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.toc-item a:hover {
  background: #f1f8e9;
  color: #4CAF50;
  transform: translateX(4px);
}

.toc-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #e4e7ed;
  color: #606266;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  margin-right: 10px;
  flex-shrink: 0;
}

.toc-item a:hover .toc-number {
  background: #4CAF50;
  color: white;
}

.page-footer {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 40px 0;
  margin-top: 60px;
}

.footer-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-content p {
  margin: 0 0 24px 0;
  color: #606266;
  font-size: 16px;
}

.footer-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
    max-width: 800px;
  }

  .table-of-contents {
    position: static;
    margin-top: 40px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 40px 0 30px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .page-subtitle {
    font-size: 14px;
  }

  .meta-info {
    flex-direction: column;
    gap: 8px;
    font-size: 12px;
  }

  .privacy-content {
    padding: 24px;
  }

  .section-title {
    font-size: 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .section-number {
    margin-right: 0;
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .section-content p {
    font-size: 14px;
    line-height: 1.7;
  }

  .subsection-title {
    font-size: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .subsection-number {
    margin-right: 0;
    width: 24px;
    height: 24px;
    font-size: 12px;
  }

  .subsection-content p {
    font-size: 14px;
  }

  .privacy-list li {
    font-size: 14px;
  }

  .table-of-contents {
    padding: 20px;
  }

  .footer-actions {
    flex-direction: column;
    gap: 12px;
  }

  .footer-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 30px 0 20px;
  }

  .page-header h1 {
    font-size: 20px;
  }

  .privacy-content {
    padding: 20px 16px;
  }

  .section {
    margin-bottom: 30px;
  }

  .section-title {
    font-size: 18px;
  }

  .section-number {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }

  .subsection-title {
    font-size: 15px;
  }

  .subsection-number {
    width: 20px;
    height: 20px;
    font-size: 11px;
  }
}

/* 打印样式 */
@media print {
  .privacy-page {
    background: white;
  }

  .page-header {
    background: white !important;
    color: black !important;
    padding: 20px 0;
  }

  .back-btn,
  .table-of-contents,
  .page-footer {
    display: none;
  }

  .content-container {
    grid-template-columns: 1fr;
    margin: 20px auto;
  }

  .privacy-content {
    box-shadow: none;
    padding: 20px;
  }

  .section-title {
    border-bottom: 2px solid black;
  }

  .section-number {
    background: black !important;
  }
}
</style>