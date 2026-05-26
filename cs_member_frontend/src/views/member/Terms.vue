<template>
  <div class="terms-page">
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
        <h1>用户服务协议</h1>
        <p class="page-subtitle">欢迎使用便利店会员管理系统，请仔细阅读以下用户服务协议</p>
        <div class="meta-info">
          <span class="update-date">最后更新：2025年1月18日</span>
          <span class="version">版本：V1.0</span>
        </div>
      </div>
    </div>

    <div class="content-container">
      <div class="terms-content">
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

                  <ul v-if="subsection.list" class="terms-list">
                    <li v-for="(item, listIndex) in subsection.list" :key="listIndex" v-html="item"></li>
                  </ul>
                </div>
              </div>
            </div>

            <ul v-if="section.list" class="terms-list">
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
        <p>本协议自用户注册成功之日起生效，长期有效</p>
        <div class="footer-actions">
          <el-button @click="printPage">
            <el-icon><Printer /></el-icon>
            打印协议
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

// 用户服务协议内容
const sections: Section[] = [
  {
    title: '协议的接受',
    content: [
      '欢迎注册成为便利店会员管理系统用户（以下简称"本系统"）。请您仔细阅读本《用户服务协议》（以下简称"本协议"），确认您在完全同意本协议所有条款的基础上，注册成为本系统用户。',
      '本协议是您与本系统之间关于注册、使用本系统服务所订立的协议。本系统有权根据需要不时地修改本协议条款，修改后的协议将在系统中公示。如果您不同意相关变更，请停止使用本系统服务。'
    ]
  },
  {
    title: '用户注册',
    content: [
      '用户注册时必须提供真实、准确、完整的个人信息，包括但不限于真实姓名、手机号码、电子邮箱地址等。如用户提供的信息不真实、不准确或不完整，本系统有权暂停或终止为其提供服务。'
    ],
    subsections: [
      {
        title: '账号安全',
        content: [
          '用户有责任保护自己的账号安全，包括但不限于妥善保管密码、不在公共设备上保存登录信息等。因用户原因导致账号被盗用、密码泄露等造成的损失，本系统不承担责任。'
        ]
      },
      {
        title: '信息真实性',
        content: [
          '用户承诺提供的个人信息真实有效，手机号和邮箱将作为用户的唯一标识，一旦注册成功原则上不予修改。如发现信息虚假，本系统有权立即终止服务。'
        ]
      }
    ]
  },
  {
    title: '用户权利和义务',
    content: [
      '用户有权使用本系统提供的各项服务功能，包括会员信息查询、消费记录查看、积分管理、优惠券使用等。'
    ],
    list: [
      '遵守国家相关法律法规和本协议规定，不得利用本系统从事违法违规活动',
      '尊重其他用户和系统运营方的合法权益，不得发布违法、违规、侵权或不当内容',
      '保护个人账号和密码安全，不得将账号转让、出借或共享给他人使用',
      '提供真实、准确的个人信息，并及时更新信息变化',
      '按照系统规定合理使用服务，不得进行恶意攻击、干扰系统正常运行等行为',
      '如发现任何安全漏洞或系统问题，应立即通知本系统运营方'
    ]
  },
  {
    title: '服务内容',
    content: [
      '本系统为便利店会员提供便捷的会员管理服务，包括但不限于：'
    ],
    list: [
      '<strong>会员注册与管理：</strong>用户信息维护、会员等级管理',
      '<strong>消费记录查询：</strong>消费明细、消费统计、消费趋势分析',
      '<strong>积分系统：</strong>积分累积、积分兑换、积分规则查询',
      '<strong>优惠券管理：</strong>优惠券领取、使用、查询',
      '<strong>优惠活动：</strong>促销信息推送、活动参与',
      '<strong>消息通知：</strong>优惠提醒、积分变动通知、系统公告'
    ]
  },
  {
    title: '用户隐私保护',
    content: [
      '本系统重视用户隐私保护，将按照相关法律法规和《隐私政策》的要求，严格保护用户个人信息安全。',
      '除法律法规要求或用户明确同意外，本系统不会向任何第三方泄露、出售或非法提供用户个人信息。'
    ]
  },
  {
    title: '知识产权',
    content: [
      '本系统的所有内容，包括但不限于文字、图片、音频、视频、软件、程序、版面设计、商标等，均受知识产权法律法规保护。',
      '未经本系统书面许可，用户不得复制、传播、展示、镜像、上载、下载本系统内容。'
    ]
  },
  {
    title: '违约处理',
    content: [
      '如果用户违反本协议规定，本系统有权采取以下措施：'
    ],
    list: [
      '<strong>警告提醒：</strong>对违规行为进行提醒和警告',
      '<strong>功能限制：</strong>限制或禁止使用部分或全部功能',
      '<strong>账号暂停：</strong>暂时停止账号使用权',
      '<strong>账号注销：</strong>永久终止服务并删除账号信息',
      '<strong>法律追究：</strong>对造成严重损失的行为保留追究法律责任的权利'
    ]
  },
  {
    title: '协议的变更和终止',
    content: [
      '本系统有权根据业务发展需要修改本协议条款，修改后的协议将在系统中公告。用户继续使用本系统服务，即视为同意修改后的协议。',
      '用户有权随时停止使用本系统服务，并可以通过系统提供的方式申请注销账号。账号注销后，用户将无法使用相关服务，相关数据也将按照隐私政策规定进行处理。'
    ]
  },
  {
    title: '争议解决',
    content: [
      '本协议的解释、效力及纠纷解决均适用中华人民共和国法律。',
      '如用户与本系统发生任何争议，应首先通过友好协商解决；协商不成的，任何一方均有权向本系统运营方所在地有管辖权的人民法院提起诉讼。'
    ]
  },
  {
    title: '其他条款',
    content: [
      '本协议自用户注册成功之日起生效，长期有效。',
      '本协议条款的标题仅为方便阅读而设，不影响本协议任何条款的解释。',
      '如本协议条款与国家法律法规相冲突，以国家法律法规为准。'
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
.terms-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.terms-content {
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
  border-bottom: 3px solid #409eff;
}

.section-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
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
  color: #409eff;
  margin: 0 0 16px 0;
}

.subsection-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #409eff;
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

.terms-list {
  margin: 16px 0;
  padding-left: 24px;
}

.terms-list li {
  margin-bottom: 12px;
  line-height: 1.7;
  color: #4a5568;
  font-size: 15px;
  list-style-type: none;
  position: relative;
  padding-left: 20px;
}

.terms-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #409eff;
  font-weight: bold;
  font-size: 18px;
}

.terms-list li:last-child {
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
  background: #f8f9ff;
  color: #409eff;
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
  background: #409eff;
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

  .terms-content {
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

  .terms-list li {
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

  .terms-content {
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
  .terms-page {
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

  .terms-content {
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