<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="480px"
    :before-close="handleClose"
    class="terms-link-dialog"
    destroy-on-close
    align-center
    center
  >
    <div class="link-content">
      <div class="link-icon">
        <el-icon size="48" color="#409eff">
          <Document />
        </el-icon>
      </div>
      <h3>{{ title }}</h3>
      <p class="link-description">
        请在新打开的页面中仔细阅读{{ title }}，阅读完毕后返回本页面继续操作。
      </p>

      <div class="link-actions">
        <el-button
          type="primary"
          size="large"
          @click="openTermsPage"
          class="open-link-btn"
        >
          <el-icon><Link /></el-icon>
          打开{{ title }}页面
        </el-button>
      </div>

      <div class="agreement-section">
        <el-checkbox v-model="agreed" size="default" class="agree-checkbox">
          我已仔细阅读并完全同意以上{{ title }}
        </el-checkbox>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :disabled="!agreed"
          @click="handleAgree"
        >
          同意并继续
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Document, Link } from '@element-plus/icons-vue'

interface SectionContent {
  title: string
  content: string[]
  subsections?: Subsection[]
  list?: string[]
}

interface Subsection {
  title: string
  content: string[]
  list?: string[]
}

interface TermsContent {
  sections: SectionContent[]
}

interface Props {
  visible: boolean
  type: 'terms' | 'privacy'
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'agree', type: 'terms' | 'privacy'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const agreed = ref(false)

const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

const title = computed(() => props.type === 'terms' ? '用户服务协议' : '隐私政策')
const subtitle = computed(() =>
  props.type === 'terms'
    ? '欢迎使用便利店会员管理系统，请仔细阅读以下用户服务协议'
    : '我们重视您的隐私保护，请仔细阅读以下隐私政策'
)
const updateTime = computed(() => '2025年1月18日')
const version = computed(() => 'V1.0')

// 用户服务协议内容
const termsContent: TermsContent = {
  sections: [
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
}

// 隐私政策内容
const privacyContent: TermsContent = {
  sections: [
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
}

const sections = computed(() => props.type === 'terms' ? termsContent.sections : privacyContent.sections)

// 监听对话框打开状态，重置同意状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    agreed.value = false
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const openTermsPage = () => {
  const route = props.type === 'terms' ? '/terms' : '/privacy'
  const baseUrl = window.location.origin
  window.open(`${baseUrl}${route}`, '_blank', 'width=800,height=600,scrollbars=yes,resizable=yes')
}

const handleAgree = () => {
  emit('agree', props.type)
  handleClose()
}
</script>

<style scoped>
/* 链接弹窗样式 */
.terms-link-dialog :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  margin: 20px auto;
  max-width: 500px;
  width: 480px;
}

.terms-link-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  margin: 0;
  border-radius: 12px 12px 0 0;
}

.terms-link-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.terms-link-dialog :deep(.el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
}

.terms-link-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 18px;
}

.terms-link-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.link-content {
  padding: 32px 24px;
  text-align: center;
  background: #fafbfc;
}

.link-icon {
  margin-bottom: 20px;
}

.link-content h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.link-description {
  margin: 0 0 32px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.link-actions {
  margin-bottom: 24px;
}

.open-link-btn {
  width: 100%;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.open-link-btn:hover {
  background: linear-gradient(135deg, #337ecc 0%, #2b8ae0 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.agreement-section {
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.agree-checkbox {
  font-size: 14px;
  color: #606266;
  width: 100%;
}

.agree-checkbox :deep(.el-checkbox__label) {
  font-weight: 500;
  line-height: 1.5;
  white-space: normal;
  word-break: break-word;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .terms-link-dialog :deep(.el-dialog) {
    width: 95%;
    max-width: 420px;
  }

  .link-content {
    padding: 24px 20px;
  }

  .link-content h3 {
    font-size: 18px;
  }

  .link-description {
    font-size: 13px;
    margin-bottom: 24px;
  }

  .open-link-btn {
    font-size: 15px;
    padding: 12px 16px;
  }

  .agreement-section {
    padding: 16px;
  }

  .agree-checkbox {
    font-size: 13px;
  }

  .dialog-footer {
    padding: 16px 20px;
    flex-direction: column;
  }

  .dialog-footer .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .terms-link-dialog :deep(.el-dialog) {
    width: 98%;
    max-width: 380px;
  }

  .link-content {
    padding: 20px 16px;
  }

  .link-content h3 {
    font-size: 17px;
  }

  .link-description {
    font-size: 12px;
    margin-bottom: 20px;
  }

  .open-link-btn {
    font-size: 14px;
    padding: 11px 16px;
  }

  .agreement-section {
    padding: 14px 12px;
  }

  .agree-checkbox {
    font-size: 12px;
  }

  .dialog-footer {
    padding: 12px 16px;
    gap: 8px;
  }
}
</style>