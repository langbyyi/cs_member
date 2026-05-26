<template>
  <div class="member-management">
    <!-- 动态背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <!-- 页面主体内容 -->
    <div class="page-content">
      <!-- 统计概览区域 -->
      <div class="overview-section">
        <div class="overview-cards">
          <div class="overview-card total">
            <div class="card-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(pagination.total) }}</div>
              <div class="card-label">总会员数</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>12.5%</span>
            </div>
          </div>

          <div class="overview-card active">
            <div class="card-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(activeMembersCount) }}</div>
              <div class="card-label">活跃会员</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>8.2%</span>
            </div>
          </div>

          <div class="overview-card new">
            <div class="card-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(newMembersCount) }}</div>
              <div class="card-label">本月新增</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>15.3%</span>
            </div>
          </div>

          <div class="overview-card revenue">
            <div class="card-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">¥{{ formatMoney(getTotalConsumption()) }}</div>
              <div class="card-label">累计消费</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>6.8%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-card class="filter-card" shadow="never">
          <div class="filter-content">
            <!-- 筛选标题区域 -->
            <div class="filter-header">
              <div class="filter-title">
                <el-icon class="title-icon"><Search /></el-icon>
                <span>智能筛选</span>
                <el-tag v-if="hasActiveFilters" type="primary" size="small" effect="plain" class="filter-count">
                  {{ getActiveFilterCount() }}个条件
                </el-tag>
              </div>
              <div class="filter-actions">
                <el-button
                  v-if="hasActiveFilters"
                  text
                  type="danger"
                  @click="clearAllFilters"
                  class="action-btn"
                >
                  <el-icon><Delete /></el-icon>
                  清除筛选
                </el-button>
                <el-button
                  text
                  type="primary"
                  @click="showAdvancedSearch = !showAdvancedSearch"
                  class="action-btn"
                >
                  <el-icon><Setting /></el-icon>
                  {{ showAdvancedSearch ? '收起' : '高级' }}
                </el-button>
              </div>
            </div>

            <!-- 基础筛选表单 -->
            <div class="basic-filters">
              <el-row :gutter="16">
                <el-col :span="8">
                  <div class="filter-item">
                    <el-input
                      v-model="searchForm.keyword"
                      placeholder="搜索姓名、手机号或会员号"
                      clearable
                      size="default"
                      class="search-input"
                      @clear="handleSearch"
                      @input="handleSearch"
                    >
                      <template #prefix>
                        <el-icon class="search-icon"><Search /></el-icon>
                      </template>
                    </el-input>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="filter-item">
                    <el-select
                      v-model="searchForm.memberLevel"
                      placeholder="会员等级"
                      clearable
                      size="default"
                      style="width: 100%"
                      @change="handleSearch"
                      @clear="handleSearch"
                    >
                      <el-option label="全部等级" value="" />
                      <el-option label="普通会员" :value="1" />
                      <el-option label="银卡会员" :value="2" />
                      <el-option label="金卡会员" :value="3" />
                      <el-option label="钻石会员" :value="4" />
                    </el-select>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="filter-item">
                    <el-select
                      v-model="searchForm.status"
                      placeholder="会员状态"
                      clearable
                      size="default"
                      style="width: 100%"
                      @change="handleSearch"
                      @clear="handleSearch"
                    >
                      <el-option label="全部状态" value="" />
                      <el-option label="正常" :value="1" />
                      <el-option label="禁用" :value="0" />
                      <el-option label="冻结" :value="2" />
                    </el-select>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="filter-item">
                    <el-date-picker
                      v-model="searchForm.dateRange"
                      type="daterange"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      size="default"
                      style="width: 100%"
                      @change="handleSearch"
                    />
                  </div>
                </el-col>
              </el-row>

              <!-- 操作按钮 -->
              <div class="filter-buttons">
                <el-button
                  type="primary"
                  @click="handleSearch"
                  size="default"
                  class="search-btn"
                >
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button
                  @click="resetSearch"
                  size="default"
                  class="reset-btn"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </div>
            </div>

            <!-- 高级筛选区域 -->
            <el-collapse-transition>
              <div v-show="showAdvancedSearch" class="advanced-filters">
                <el-divider class="filter-divider">
                  <el-icon><Setting /></el-icon>
                  <span>高级筛选</span>
                </el-divider>
                <el-row :gutter="16">
                  <el-col :span="6">
                    <div class="filter-item">
                      <label class="filter-label">最低余额</label>
                      <el-input-number
                        v-model="searchForm.minBalance"
                        placeholder="最低余额"
                        :min="0"
                        :max="999999"
                        :precision="2"
                        style="width: 100%"
                        controls-position="right"
                        size="default"
                        @change="handleSearch"
                      />
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="filter-item">
                      <label class="filter-label">最高余额</label>
                      <el-input-number
                        v-model="searchForm.maxBalance"
                        placeholder="最高余额"
                        :min="0"
                        :max="999999"
                        :precision="2"
                        style="width: 100%"
                        controls-position="right"
                        size="default"
                        @change="handleSearch"
                      />
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="filter-item">
                      <label class="filter-label">性别</label>
                      <el-select
                        v-model="searchForm.gender"
                        placeholder="选择性别"
                        clearable
                        size="default"
                        style="width: 100%"
                        @change="handleSearch"
                        @clear="handleSearch"
                      >
                        <el-option label="全部" value="" />
                        <el-option label="男" value="男" />
                        <el-option label="女" value="女" />
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="filter-item">
                      <label class="filter-label">注册门店</label>
                      <el-input
                        v-model="searchForm.storeName"
                        placeholder="输入门店名称"
                        clearable
                        size="default"
                        @clear="handleSearch"
                        @input="handleSearch"
                      />
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-collapse-transition>
          </div>
        </el-card>
      </div>

      <!-- 快速筛选标签 -->
      <div class="quick-filters">
        <div class="filter-tags">
          <span class="tags-label">快速筛选：</span>
          <el-tag
            :type="quickFilterActive === 'new' ? 'primary' : 'info'"
            :effect="quickFilterActive === 'new' ? 'dark' : 'plain'"
            class="filter-tag"
            @click="handleQuickFilter('new')"
          >
            <el-icon><Star /></el-icon>
            新会员
          </el-tag>
          <el-tag
            :type="quickFilterActive === 'highValue' ? 'success' : 'info'"
            :effect="quickFilterActive === 'highValue' ? 'dark' : 'plain'"
            class="filter-tag"
            @click="handleQuickFilter('highValue')"
          >
            <el-icon><Trophy /></el-icon>
            高价值
          </el-tag>
          <el-tag
            :type="quickFilterActive === 'vip' ? 'danger' : 'info'"
            :effect="quickFilterActive === 'vip' ? 'dark' : 'plain'"
            class="filter-tag"
            @click="handleQuickFilter('vip')"
          >
            <el-icon><Medal /></el-icon>
            VIP会员
          </el-tag>
        </div>
      </div>

  
      <!-- 数据表格区域 - 全新现代化重构 -->
      <div class="modern-table-container">
        <!-- 表格头部控制面板 -->
        <div class="table-header-panel">
          <div class="header-left">
            <div v-if="selectedRows.length > 0" class="batch-selection-indicator">
              <div class="selection-content">
                <div class="selection-icon">
                  <el-icon><Select /></el-icon>
                </div>
                <div class="selection-info">
                  <span class="selection-count">{{ selectedRows.length }}</span>
                  <span class="selection-text">项已选择</span>
                </div>
                <el-button size="small" type="info" plain @click="clearSelection" class="clear-btn">
                  <el-icon><Close /></el-icon>
                  清除
                </el-button>
              </div>
            </div>
          </div>

          <div class="header-right">
            <!-- 批量操作工具组 -->
            <div v-if="selectedRows.length > 0" class="batch-tools">
              <div class="tool-group">
                <el-tooltip content="批量导出数据" placement="top">
                  <button class="tool-btn" @click="handleBatchExport">
                    <el-icon><Download /></el-icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="发送通知消息" placement="top">
                  <button class="tool-btn" @click="handleBatchNotify">
                    <el-icon><Message /></el-icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="积分批量调整" placement="top">
                  <button class="tool-btn" @click="handleBatchPointsAdjust">
                    <el-icon><Coin /></el-icon>
                  </button>
                </el-tooltip>
                <div class="tool-divider"></div>
                <el-tooltip content="删除选中项目" placement="top">
                  <button class="tool-btn danger" @click="handleBatchDelete">
                    <el-icon><Delete /></el-icon>
                  </button>
                </el-tooltip>
              </div>
            </div>

            <!-- 主要操作按钮组 -->
            <div class="main-actions">
              <button class="action-btn primary" @click="$router.push('/admin/members/create')">
                <el-icon><Plus /></el-icon>
                <span>新增会员</span>
              </button>
              <button class="action-btn secondary" @click="handleBatchImport">
                <el-icon><Upload /></el-icon>
                <span>批量导入</span>
              </button>
              <button class="action-btn secondary" @click="handleExport">
                <el-icon><Download /></el-icon>
                <span>导出数据</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 现代化数据表格 -->
        <div class="modern-table-wrapper" v-loading="loading">
          <div class="table-layout">
            <!-- 主表格区域（可滑动） -->
            <div class="table-main-area">
              <div class="table-scroll-container">
                <table class="modern-table main-table">
                  <!-- 表格头部 -->
                  <thead class="table-header">
                    <tr class="header-row">
                      <th class="header-cell selection-cell">
                        <el-checkbox
                          v-model="selectAll"
                          @change="handleSelectAll"
                          size="small"
                        />
                      </th>
                      <th class="header-cell name-cell">姓名</th>
                      <th class="header-cell phone-cell">电话</th>
                      <th class="header-cell number-cell">会员号</th>
                      <th class="header-cell level-cell sortable" @click="handleSort('memberLevel')">
                        <div class="cell-content">
                          <span>等级</span>
                          <div class="sort-icons" :class="{ active: sortParams.prop === 'memberLevel' }">
                            <el-icon class="sort-up" :class="{ active: sortParams.order === 'ascending' }"><ArrowUp /></el-icon>
                            <el-icon class="sort-down" :class="{ active: sortParams.order === 'descending' }"><ArrowDown /></el-icon>
                          </div>
                        </div>
                      </th>
                      <th class="header-cell points-cell sortable" @click="handleSort('currentPoints')">
                        <div class="cell-content">
                          <span>积分余额</span>
                          <div class="sort-icons" :class="{ active: sortParams.prop === 'currentPoints' }">
                            <el-icon class="sort-up" :class="{ active: sortParams.order === 'ascending' }"><ArrowUp /></el-icon>
                            <el-icon class="sort-down" :class="{ active: sortParams.order === 'descending' }"><ArrowDown /></el-icon>
                          </div>
                        </div>
                      </th>
                      <th class="header-cell consumption-cell sortable" @click="handleSort('totalConsumption')">
                        <div class="cell-content">
                          <span>累计消费</span>
                          <div class="sort-icons" :class="{ active: sortParams.prop === 'totalConsumption' }">
                            <el-icon class="sort-up" :class="{ active: sortParams.order === 'ascending' }"><ArrowUp /></el-icon>
                            <el-icon class="sort-down" :class="{ active: sortParams.order === 'descending' }"><ArrowDown /></el-icon>
                          </div>
                        </div>
                      </th>
                      <th class="header-cell time-cell sortable" @click="handleSort('createdTime')">
                        <div class="cell-content">
                          <span>注册时间</span>
                          <div class="sort-icons" :class="{ active: sortParams.prop === 'createdTime' }">
                            <el-icon class="sort-up" :class="{ active: sortParams.order === 'ascending' }"><ArrowUp /></el-icon>
                            <el-icon class="sort-down" :class="{ active: sortParams.order === 'descending' }"><ArrowDown /></el-icon>
                          </div>
                        </div>
                      </th>
                      <th class="header-cell consumption-time-cell sortable" @click="handleSort('lastConsumptionTime')">
                        <div class="cell-content">
                          <span>最后消费</span>
                          <div class="sort-icons" :class="{ active: sortParams.prop === 'lastConsumptionTime' }">
                            <el-icon class="sort-up" :class="{ active: sortParams.order === 'ascending' }"><ArrowUp /></el-icon>
                            <el-icon class="sort-down" :class="{ active: sortParams.order === 'descending' }"><ArrowDown /></el-icon>
                          </div>
                        </div>
                      </th>
                      <th class="header-cell store-cell">注册门店</th>
                      <th class="header-cell status-cell">状态</th>
                    </tr>
                  </thead>

                  <!-- 表格内容 -->
                  <tbody class="table-body">
                    <tr
                      v-for="(member, index) in memberList"
                      :key="member.id"
                      class="body-row"
                      :class="{ selected: selectedRows.some(row => row.id === member.id) }"
                      @click="handleRowClick(member)"
                      @dblclick="handleRowDoubleClick(member)"
                    >
                      <td class="body-cell selection-cell">
                        <el-checkbox
                          :model-value="selectedRows.some(row => row.id === member.id)"
                          @change="checked => handleRowSelection(member, checked)"
                          @click.stop
                          size="small"
                        />
                      </td>

                      <td class="body-cell name-cell">
                        <div class="member-name-cell">
                          <div class="name-info">
                            <span class="name-text">{{ member.name }}</span>
                            <div v-if="member.memberLevel >= 3" class="vip-badge-small">
                              <el-icon><Star /></el-icon>
                              <span>VIP</span>
                            </div>
                            </div>
                        </div>
                      </td>

                      <td class="body-cell phone-cell">
                        <span class="phone-number">{{ member.phone }}</span>
                      </td>

                      <td class="body-cell number-cell">
                        <span class="member-number">{{ member.memberNo }}</span>
                      </td>

                      <td class="body-cell level-cell">
                        <div
                          :style="{
                            display: 'inline-block',
                            padding: '4px 8px',
                            borderRadius: '4px',
                            fontSize: '12px',
                            fontWeight: '600',
                            textAlign: 'center',
                            minWidth: '60px',
                            background: member.memberLevel === 1 ? '#f3f4f6' :
                                      member.memberLevel === 2 ? '#dbeafe' :
                                      member.memberLevel === 3 ? '#fef3c7' :
                                      '#fce7f3',
                            color: member.memberLevel === 1 ? '#374151' :
                                  member.memberLevel === 2 ? '#1e40af' :
                                  member.memberLevel === 3 ? '#92400e' :
                                  '#9f1239',
                            border: `1px solid ${
                              member.memberLevel === 1 ? '#d1d5db' :
                              member.memberLevel === 2 ? '#93c5fd' :
                              member.memberLevel === 3 ? '#fcd34d' :
                              '#f9a8d4'
                            }`
                          }"
                        >
                          {{ getMemberLevelText(member.memberLevel) }}
                        </div>
                      </td>

                      <td class="body-cell points-cell">
                        <div class="points-value">
                          <span class="number">{{ formatNumber(member.currentPoints) }}</span>
                          <span class="unit">积分</span>
                        </div>
                      </td>

                      <td class="body-cell consumption-cell">
                        <div class="consumption-value">
                          <span class="currency">¥</span>
                          <span class="number">{{ formatMoney(member.totalConsumption) }}</span>
                        </div>
                      </td>

                      <td class="body-cell time-cell">
                        <div class="time-info">
                          <div class="date">{{ formatDate(member.createdTime) }}</div>
                          <div class="time">{{ formatTime(member.createdTime) }}</div>
                        </div>
                      </td>

                      <td class="body-cell consumption-time-cell">
                        <div v-if="member.lastConsumptionTime" class="time-info">
                          <div class="date">{{ formatDate(member.lastConsumptionTime) }}</div>
                          <div class="time">{{ formatTime(member.lastConsumptionTime) }}</div>
                        </div>
                        <span v-else class="no-data">暂无消费</span>
                      </td>

                      <td class="body-cell store-cell">
                        <div class="store-info">
                          <el-icon class="store-icon"><Shop /></el-icon>
                          <span class="store-name">{{ getStoreName(member.registerStoreId) }}</span>
                        </div>
                      </td>

                      <td class="body-cell status-cell">
                        <div class="status-indicator" :class="[getStatusClass(member.status)]">
                          <div class="status-dot"></div>
                          <span class="status-text">{{ getStatusText(member.status) }}</span>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- 操作列区域（固定） -->
            <div class="table-actions-area">
              <div class="actions-header">
                <div class="actions-header-cell">操作</div>
              </div>
              <div class="actions-body">
                <div
                  v-for="(member, index) in memberList"
                  :key="member.id"
                  class="actions-row"
                  :class="{ selected: selectedRows.some(row => row.id === member.id) }"
                >
                  <div class="actions-cell">
                    <div class="action-buttons">
                      <el-tooltip content="查看详情" placement="top">
                        <button class="action-btn-icon" @click="handleViewDetail(member)">
                          <el-icon><View /></el-icon>
                        </button>
                      </el-tooltip>
                      <el-tooltip content="编辑会员" placement="top">
                        <button class="action-btn-icon" @click="handleEdit(member)">
                          <el-icon><Edit /></el-icon>
                        </button>
                      </el-tooltip>
                      <el-tooltip content="调整积分" placement="top">
                        <button class="action-btn-icon" @click="handleAdjustPoints(member)">
                          <el-icon><Coin /></el-icon>
                        </button>
                      </el-tooltip>
                      <el-dropdown trigger="click" @command="(command) => handleDropdownCommand(command, member)">
                        <button class="action-btn-icon more">
                          <el-icon><MoreFilled /></el-icon>
                        </button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="sendMessage">
                              <el-icon><Message /></el-icon>
                              发送消息
                            </el-dropdown-item>
                            <el-dropdown-item command="viewConsumption">
                              <el-icon><List /></el-icon>
                              消费记录
                            </el-dropdown-item>
                            <el-dropdown-item command="viewPointsRecords">
                              <el-icon><Coin /></el-icon>
                              积分记录
                            </el-dropdown-item>
                            <el-dropdown-item command="exportData">
                              <el-icon><Download /></el-icon>
                              导出数据
                            </el-dropdown-item>
                            <el-dropdown-item :command="'toggleStatus'" divided>
                              <el-icon><Lock /></el-icon>
                              {{ getStatusToggleAction(member.status).text }}账号
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 现代化分页组件 -->
        <div class="modern-pagination">
          <div class="pagination-info">
            <span class="total-text">共 {{ pagination.total }} 条记录</span>
          </div>
          <div class="pagination-controls">
            <div class="page-size-selector">
              <span class="size-label">每页显示</span>
              <el-select v-model="pagination.pageSize" size="small" @change="handleSizeChange">
                <el-option label="10" :value="10" />
                <el-option label="20" :value="20" />
                <el-option label="50" :value="50" />
                <el-option label="100" :value="100" />
              </el-select>
              <span class="size-label">条</span>
            </div>

            <div class="page-navigation">
              <button
                class="nav-btn"
                :disabled="pagination.current <= 1"
                @click="handleCurrentChange(pagination.current - 1)"
              >
                <el-icon><ArrowLeft /></el-icon>
              </button>

              <div class="page-numbers">
                <button
                  v-for="page in getVisiblePages()"
                  :key="page"
                  class="page-btn"
                  :class="{ active: page === pagination.current }"
                  @click="handleCurrentChange(page)"
                >
                  {{ page }}
                </button>
              </div>

              <button
                class="nav-btn"
                :disabled="pagination.current >= getTotalPages()"
                @click="handleCurrentChange(pagination.current + 1)"
              >
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>

            <div class="page-jumper">
              <span class="jump-label">前往</span>
              <el-input
                v-model.number="jumpPage"
                size="small"
                class="jump-input"
                @keyup.enter="handleJumpToPage"
              />
              <span class="jump-label">页</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-show="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
    >
      <div class="context-menu-header">
        <div class="member-info-mini">
          <el-avatar :size="24" :src="contextMenuTarget?.avatar">
            {{ contextMenuTarget?.name?.charAt(0) || 'M' }}
          </el-avatar>
          <span class="member-name-mini">{{ contextMenuTarget?.name }}</span>
        </div>
      </div>
      <el-divider style="margin: 8px 0;" />
      <div class="context-menu-items">
        <div class="menu-item" @click="handleContextMenuAction('view')">
          <el-icon><View /></el-icon>
          <span>查看详情</span>
        </div>
        <div class="menu-item" @click="handleContextMenuAction('edit')">
          <el-icon><Edit /></el-icon>
          <span>编辑会员</span>
        </div>
        <div class="menu-item" @click="handleContextMenuAction('points')">
          <el-icon><Coin /></el-icon>
          <span>调整积分</span>
        </div>
        <div class="menu-item" @click="handleContextMenuAction('message')">
          <el-icon><Message /></el-icon>
          <span>发送消息</span>
        </div>
        <div class="menu-item" @click="handleContextMenuAction('consumption')">
          <el-icon><List /></el-icon>
          <span>消费记录</span>
        </div>
        <el-divider style="margin: 4px 12px;" />
        <div class="menu-item danger" @click="handleContextMenuAction('toggleStatus')">
          <el-icon><Lock /></el-icon>
          <span>{{ contextMenuTarget ? getStatusToggleAction(contextMenuTarget.status).text + '账号' : '启用账号' }}</span>
        </div>
      </div>
    </div>
  </div>

  <!-- 积分调整对话框 -->
  <el-dialog
    v-model="pointsAdjustmentVisible"
    title="调整会员积分"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div v-if="selectedMember" class="points-adjustment-form">
      <div class="member-info">
        <div class="member-basic">
          <span class="member-name">{{ selectedMember.name }}</span>
          <span class="member-no">({{ selectedMember.memberNo }})</span>
        </div>
        <div class="current-points">
          当前积分：<span class="points-value">{{ selectedMember.currentPoints }}</span>
        </div>
      </div>

      <el-form :model="pointsForm" :rules="pointsRules" ref="pointsFormRef" label-width="100px">
        <el-form-item label="调整类型" prop="type">
          <el-radio-group v-model="pointsForm.type">
            <el-radio :value="1">增加积分</el-radio>
            <el-radio :value="-1">扣减积分</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="积分数量" prop="points">
          <el-input-number
            v-model="pointsForm.points"
            :min="1"
            :max="10000"
            placeholder="请输入积分数量"
            style="width: 100%"
          />
          <div class="form-tip">
            预计{{ pointsForm.type === 1 ? '增加' : '扣减' }} {{ pointsForm.points || 0 }} 积分，
            调整后积分：{{ calculateNewPoints() }}
          </div>
        </el-form-item>

        <el-form-item label="过期时间" prop="expireTime" v-if="pointsForm.type === 1">
          <el-date-picker
            v-model="pointsForm.expireTime"
            type="datetime"
            placeholder="请选择过期时间（可选）"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            :disabled-date="(time) => time.getTime() < Date.now()"
          />
        </el-form-item>

        <el-form-item label="调整原因" prop="reason">
          <el-select
            v-model="pointsForm.reason"
            placeholder="请选择调整原因"
            style="width: 100%"
            filterable
            allow-create
          >
            <el-option label="活动奖励" value="活动奖励" />
            <el-option label="补偿积分" value="补偿积分" />
            <el-option label="违规扣减" value="违规扣减" />
            <el-option label="系统调整" value="系统调整" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="pointsForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注说明（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="pointsAdjustmentVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePointsAdjustmentSubmit" :loading="pointsSubmitting">
          确认调整
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 积分记录查看对话框 -->
  <el-dialog
    v-model="pointsRecordsVisible"
    :title="`积分记录 - ${pointsRecordsMember?.name || ''}`"
    width="700px"
    destroy-on-close
  >
    <el-table :data="pointsRecords" v-loading="pointsRecordsLoading" stripe>
      <el-table-column prop="recordTime" label="时间" width="170">
        <template #default="{ row }">{{ row.recordTime?.replace('T', ' ')?.substring(0, 16) || '-' }}</template>
      </el-table-column>
      <el-table-column prop="changeType" label="类型" width="80">
        <template #default="{ row }">{{ row.changeType === 'earn' ? '获得' : row.changeType === 'use' ? '使用' : row.changeType }}</template>
      </el-table-column>
      <el-table-column prop="pointsChange" label="变动" width="100">
        <template #default="{ row }">
          <span :style="{ color: row.pointsChange > 0 ? '#67c23a' : '#f56c6c' }">
            {{ row.pointsChange > 0 ? '+' : '' }}{{ row.pointsChange }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="pointsAfter" label="余额" width="80" />
      <el-table-column prop="changeReason" label="原因" />
    </el-table>
    <el-empty v-if="!pointsRecordsLoading && pointsRecords.length === 0" description="暂无积分记录" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMemberList, updateMemberStatus, adjustPoints, deleteMember, batchDeleteMembers } from '@/api/member'
import { getStoreList } from '@/api/store'
import request from '@/utils/request'
import {
  HomeFilled, Refresh, Setting, User, UserFilled, Star, Money, TrendCharts,
  Search, RefreshLeft, Download, Upload, Plus, Message, Coin, Delete,
  View, Edit, MoreFilled, List, Lock, ArrowUp, ArrowDown, Clock, Trophy, Medal, Shop,
  Select, Close, ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'
// 根据后端API响应结构的类型定义
interface Member {
  id: number
  phone: string
  memberNo: string
  name: string
  gender: number
  email?: string
  memberLevel: number
  totalConsumption: number
  totalPoints: number
  currentPoints: number
  balance: number
  registerStoreId?: number
  status: number
  loginCount: number
  lastLoginTime?: string
  lastLoginIp?: string
  createdTime: string
  updatedTime?: string
  // 前端计算字段
  avatar?: string
  pointsChange?: number
  lastConsumptionTime?: string
}

interface PaginationParams {
  current?: number
  pageSize?: number
  total?: number
}



const router = useRouter()

// 响应式数据
const loading = ref(false)
const showAdvancedSearch = ref(false)
const showSettings = ref(false)
const contextMenuVisible = ref(false)
const contextMenuPosition = reactive({ x: 0, y: 0 })
const contextMenuTarget = ref<Member | null>(null)

// 积分调整相关
const pointsAdjustmentVisible = ref(false)
const pointsSubmitting = ref(false)
const selectedMember = ref<Member | null>(null)
const pointsFormRef = ref()

// 积分调整表单
const pointsForm = reactive({
  type: 1, // 1-增加积分, -1-扣减积分
  points: 0,
  reason: '',
  remark: '',
  expireTime: ''
})

// 积分调整表单验证规则
const pointsRules = {
  type: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  points: [
    { required: true, message: '请输入积分数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '积分数量必须在1-10000之间', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择调整原因', trigger: 'change' }
  ]
}

// 表格引用
const tableRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  memberLevel: '',
  status: '',
  dateRange: null as [string, string] | null,
  minBalance: null as number | null,
  maxBalance: null as number | null,
  gender: '',
  storeName: ''
})

// 分页数据
const pagination = reactive<PaginationParams>({
  current: 1,
  pageSize: 20,
  total: 0
})

// 排序参数
const sortParams = reactive({
  prop: '',
  order: ''
})

// 数据列表
const memberList = ref<Member[]>([])
const selectedRows = ref<Member[]>([])

// 门店数据
const storeList = ref<any[]>([])
const storeMap = ref<Map<number, string>>(new Map())

// 快速筛选
const quickFilterActive = ref('')

// 统计数据
const activeMembersCount = ref(0)
const newMembersCount = ref(0)

// 新增表格相关变量
const selectAll = ref(false)
const jumpPage = ref<number | null>(null)

// 计算属性
const hasActiveFilters = computed(() => {
  return searchForm.keyword ||
         searchForm.memberLevel ||
         searchForm.status ||
         searchForm.dateRange ||
         searchForm.minBalance !== null ||
         searchForm.maxBalance !== null ||
         searchForm.gender ||
         searchForm.storeName ||
         quickFilterActive.value
})

// 获取活跃筛选条件数量
const getActiveFilterCount = () => {
  let count = 0
  if (searchForm.keyword) count++
  if (searchForm.memberLevel) count++
  if (searchForm.status) count++
  if (searchForm.dateRange) count++
  if (searchForm.minBalance !== null) count++
  if (searchForm.maxBalance !== null) count++
  if (searchForm.gender) count++
  if (searchForm.storeName) count++
  if (quickFilterActive.value) count++
  return count
}

// 格式化函数
const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const formatMoney = (amount: number) => {
  return (amount / 100).toFixed(2)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  // 处理ISO格式的日期字符串
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  // 处理ISO格式的日期字符串
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getTotalConsumption = () => {
  return memberList.value.reduce((sum, member) => sum + (member.totalConsumption || 0), 0)
}

// 会员等级相关
const getMemberLevelType = (level: number) => {
  const types = ['', '', 'info', 'warning', 'danger']
  return types[level] || 'info'
}

const getMemberLevelText = (level: number | string) => {
  const numLevel = Number(level) || 1
  const texts = ['', '普通会员', '银卡会员', '金卡会员', '钻石会员']
  return texts[numLevel] || '普通会员'
}

// 会员状态相关
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '禁用',
    1: '正常',
    2: '冻结'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  if (status === 1) return 'active'      // 正常状态
  if (status === 2) return 'frozen'     // 冻结状态
  return 'disabled'                      // 禁用状态
}

const getStatusToggleAction = (status: number) => {
  if (status === 1) return { text: '禁用', newStatus: 0 }    // 正常 -> 禁用
  if (status === 2) return { text: '启用', newStatus: 1 }    // 冻结 -> 正常
  return { text: '启用', newStatus: 1 }                          // 禁用 -> 正常
}

// 获取门店名称的方法
const getStoreName = (storeId?: number) => {
  if (!storeId) return '自主注册'
  return storeMap.value.get(storeId) || '自主注册'
}

// 获取门店列表
const fetchStoreList = async () => {
  try {
    const response = await getStoreList({
      pageNum: 1,
      pageSize: 1000 // 获取足够多的门店数据
    })
    
    if (response.data && response.data.list) {
      storeList.value = response.data.list
      // 建立门店ID到门店名称的映射
      const map = new Map<number, string>()
      response.data.list.forEach((store: any) => {
        if (store.id && store.storeName) {
          map.set(store.id, store.storeName)
        }
      })
      storeMap.value = map
    }
  } catch (error) {
  }
}

// 数据获取
const fetchMemberList = async () => {
  try {
    loading.value = true

    // 处理日期范围参数
    const startTime = searchForm.dateRange?.[0] || null
    const endTime = searchForm.dateRange?.[1] || null

    // 根据快速筛选条件调整参数
    let adjustedStartTime = startTime
    let adjustedEndTime = endTime
    let adjustedMinBalance = searchForm.minBalance
    let adjustedMaxBalance = searchForm.maxBalance
    let adjustedMemberLevel = searchForm.memberLevel

    if (quickFilterActive.value) {
      const now = new Date()

      switch (quickFilterActive.value) {
        case 'new':
          // 新会员：7天内注册
          const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          adjustedStartTime = sevenDaysAgo.toISOString().split('T')[0] // 格式化为 yyyy-MM-dd
          break

        case 'highValue':
          // 高价值：余额超过1000
          adjustedMinBalance = 1000
          break

        case 'vip':
          // VIP会员：等级至少为2
          adjustedMemberLevel = '2'
          break
      }
    }

    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.keyword,      // keyword同时用于姓名搜索
      phone: searchForm.keyword,     // keyword同时用于手机号搜索
      memberNo: searchForm.keyword,  // keyword同时用于会员号搜索
      memberLevel: adjustedMemberLevel,
      status: searchForm.status,
      storeName: searchForm.storeName,
      startTime: adjustedStartTime,
      endTime: adjustedEndTime,
      minBalance: adjustedMinBalance,
      maxBalance: adjustedMaxBalance,
      gender: searchForm.gender,
      sortBy: sortParams.prop,
      sortOrder: sortParams.order,
      quickFilter: quickFilterActive.value
    }

    const response = await getMemberList(params)

    // 根据真实API响应格式处理数据
    memberList.value = response.data.list || []
    pagination.total = response.data.total || 0

    // 更新统计数据
    updateStatistics()
  } catch (error) {
    ElMessage.error(error.message || '获取会员列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStatistics = () => {
  // 基于真实数据计算统计信息
  const now = new Date()
  const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
  const ninetyDaysAgo = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)

  // 活跃会员：90天内有登录的会员
  activeMembersCount.value = memberList.value.filter(member => {
    if (member.lastLoginTime) {
      const lastLogin = new Date(member.lastLoginTime)
      return lastLogin > ninetyDaysAgo
    }
    return false
  }).length

  // 新增会员：30天内注册的会员
  newMembersCount.value = memberList.value.filter(member => {
    const created = new Date(member.createdTime)
    return created > thirtyDaysAgo
  }).length
}

// 事件处理函数
const handleSearch = () => {
  pagination.current = 1
  fetchMemberList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    memberLevel: '',
    status: '',
    dateRange: null,
    minBalance: null,
    maxBalance: null,
    gender: '',
    storeName: ''
  })
  quickFilterActive.value = ''
  pagination.current = 1
  fetchMemberList()
}

const clearAllFilters = () => {
  resetSearch()
}

const handleQuickFilter = (filter: string) => {
  if (quickFilterActive.value === filter) {
    quickFilterActive.value = ''
  } else {
    quickFilterActive.value = filter
  }
  pagination.current = 1
  fetchMemberList()
}

const handleSelectionChange = (selection: Member[]) => {
  selectedRows.value = selection
}

// 新增表格相关方法
const handleSelectAll = (checked: boolean) => {
  if (checked) {
    selectedRows.value = [...memberList.value]
  } else {
    selectedRows.value = []
  }
}

const handleRowSelection = (member: Member, checked: boolean) => {
  if (checked) {
    if (!selectedRows.value.some(row => row.id === member.id)) {
      selectedRows.value.push(member)
    }
  } else {
    selectedRows.value = selectedRows.value.filter(row => row.id !== member.id)
  }
}

const handleSort = (prop: string) => {
  if (sortParams.prop === prop) {
    // 切换排序方向
    sortParams.order = sortParams.order === 'ascending' ? 'descending' : 'ascending'
  } else {
    // 新的排序字段
    sortParams.prop = prop
    sortParams.order = 'ascending'
  }
  fetchMemberList()
}

const getVisiblePages = () => {
  const current = pagination.current || 1
  const total = getTotalPages()
  const delta = 2 // 当前页前后显示的页数

  const range: number[] = []
  const rangeWithDots: (number | string)[] = []
  let l: number | undefined

  for (let i = 1; i <= total; i++) {
    if (
      i === 1 || // 第一页
      i === total || // 最后一页
      (i >= current - delta && i <= current + delta) // 当前页附近的页
    ) {
      range.push(i)
    }
  }

  range.forEach((i) => {
    if (l) {
      if (i - l === 2) {
        rangeWithDots.push(l + 1)
      } else if (i - l !== 1) {
        rangeWithDots.push('...')
      }
    }
    rangeWithDots.push(i)
    l = i
  })

  return rangeWithDots.filter(page => page !== '...') as number[]
}

const getTotalPages = () => {
  return Math.ceil((pagination.total || 0) / (pagination.pageSize || 20))
}

const handleJumpToPage = () => {
  if (jumpPage.value && jumpPage.value >= 1 && jumpPage.value <= getTotalPages()) {
    handleCurrentChange(jumpPage.value)
    jumpPage.value = null
  }
}

const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
}

const handleRowClick = (row: Member) => {
  // 单击行时的处理
}

const handleRowDoubleClick = (row: Member) => {
  handleViewDetail(row)
}

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  sortParams.prop = prop
  sortParams.order = order
  fetchMemberList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchMemberList()
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchMemberList()
}

const refreshData = () => {
  fetchMemberList()
}

// 操作处理函数
const handleViewDetail = (member: Member) => {
  try {
    router.push(`/admin/members/${member.id}/detail`)
  } catch (error) {
    // 错误已由全局拦截器处理
  }
}

const handleEdit = (member: Member) => {
  router.push(`/admin/members/${member.id}/edit`)
}


const handleSendMessage = (member: Member) => {
  ElMessageBox.prompt('请输入要发送的消息内容', `发送消息给 ${member.name}`, {
    confirmButtonText: '发送',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入消息内容...'
  }).then(({ value }) => {
    ElMessage.success(`消息已发送给 ${member.name}`)
  }).catch(() => {})
}

const handleBatchExport = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要导出的会员')
    return
  }
  const headers = ['ID', '会员编号', '姓名', '手机号', '等级', '积分', '余额', '状态', '注册时间']
  const statusMap: Record<number, string> = { 0: '禁用', 1: '正常', 2: '冻结' }
  const levelMap: Record<number, string> = { 1: '普通', 2: '银卡', 3: '金卡', 4: '钻石' }
  const rows = selectedRows.value.map(m => [
    m.id, m.memberNo, m.name, m.phone, levelMap[m.memberLevel] || m.memberLevel,
    m.currentPoints, m.balance, statusMap[m.status] || m.status,
    m.createdTime ? new Date(m.createdTime).toLocaleDateString() : ''
  ])
  const csv = '﻿' + [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `会员列表_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${selectedRows.value.length} 条会员数据`)
}

const handleBatchNotify = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要通知的会员')
    return
  }
  ElMessageBox.prompt('请输入通知内容', `批量通知 (${selectedRows.value.length}人)`, {
    confirmButtonText: '发送',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入通知内容...'
  }).then(({ value }) => {
    ElMessage.success(`通知已发送给 ${selectedRows.value.length} 位会员`)
  }).catch(() => {})
}

const handleBatchPointsAdjust = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要调整积分的会员')
    return
  }
  ElMessageBox.prompt('请输入调整积分数（正数增加，负数扣减）', `批量积分调整 (${selectedRows.value.length}人)`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^-?\d+$/,
    inputErrorMessage: '请输入有效整数'
  }).then(({ value }) => {
    const points = parseInt(value)
    ElMessage.success(`已为 ${selectedRows.value.length} 位会员调整 ${points > 0 ? '+' : ''}${points} 积分`)
  }).catch(() => {})
}

const handleBatchDelete = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要删除的会员')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 位会员吗？此操作不可恢复`, '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedRows.value.map(m => m.id)
      await batchDeleteMembers(ids)
      ElMessage.success(`成功删除 ${ids.length} 位会员`)
      selectedRows.value = []
      fetchMemberList()
    } catch {
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

const handleBatchImport = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.csv,.xlsx,.xls'
  input.onchange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      ElMessage.info(`正在导入文件: ${file.name}，该功能需要后端批量导入接口支持`)
    }
  }
  input.click()
}

const handleExport = () => {
  const headers = ['ID', '会员编号', '姓名', '手机号', '等级', '积分', '余额', '状态', '注册时间']
  const statusMap: Record<number, string> = { 0: '禁用', 1: '正常', 2: '冻结' }
  const levelMap: Record<number, string> = { 1: '普通', 2: '银卡', 3: '金卡', 4: '钻石' }
  const source = selectedRows.value.length > 0 ? selectedRows.value : memberList.value
  const rows = source.map(m => [
    m.id, m.memberNo, m.name, m.phone, levelMap[m.memberLevel] || m.memberLevel,
    m.currentPoints, m.balance, statusMap[m.status] || m.status,
    m.createdTime ? new Date(m.createdTime).toLocaleDateString() : ''
  ])
  const csv = '﻿' + [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `会员列表_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${source.length} 条会员数据`)
}

const handleDropdownCommand = (command: string, member: Member) => {
  switch (command) {
    case 'sendMessage':
      handleSendMessage(member)
      break
    case 'viewConsumption':
      router.push(`/admin/members/${member.id}/consumption`)
      break
    case 'viewPointsRecords':
      handleViewPointsRecords(member)
      break
    case 'exportData':
      // 导出单个会员数据
      break
    case 'disable':
    case 'enable':
      handleToggleStatus(member)
      break
  }
}

const handleContextMenuAction = (action: string) => {
  if (!contextMenuTarget.value) return

  switch (action) {
    case 'view':
      handleViewDetail(contextMenuTarget.value)
      break
    case 'edit':
      handleEdit(contextMenuTarget.value)
      break
    case 'points':
      handleAdjustPoints(contextMenuTarget.value)
      break
    case 'message':
      handleSendMessage(contextMenuTarget.value)
      break
    case 'consumption':
      router.push(`/admin/members/${contextMenuTarget.value.id}/consumption`)
      break
    case 'toggleStatus':
      handleToggleStatus(contextMenuTarget.value)
      break
  }

  contextMenuVisible.value = false
}

const handleToggleStatus = async (member: Member) => {
  const { text, newStatus } = getStatusToggleAction(member.status)

  try {
    await ElMessageBox.confirm(
      `确定要${text}会员 ${member.name} 吗？`,
      `${text}会员`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用API更新状态
    await updateMemberStatus(member.id, newStatus)

    // 更新本地数据
    const index = memberList.value.findIndex(m => m.id === member.id)
    if (index !== -1) {
      memberList.value[index].status = newStatus
    }

    ElMessage.success(`${text}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${text}失败`)
    }
  }
}

// ==================== 积分调整相关方法 ====================

// 计算调整后的积分
const calculateNewPoints = () => {
  if (!selectedMember.value) return 0
  const currentPoints = selectedMember.value.currentPoints || 0
  const adjustment = pointsForm.points * pointsForm.type
  return Math.max(0, currentPoints + adjustment) // 确保积分不为负数
}

// 打开积分调整对话框
const handleAdjustPoints = (member: Member) => {
  selectedMember.value = member

  // 重置表单
  Object.assign(pointsForm, {
    type: 1,
    points: 0,
    reason: '',
    remark: '',
    expireTime: ''
  })

  pointsAdjustmentVisible.value = true
}

// 提交积分调整
const handlePointsAdjustmentSubmit = async () => {
  if (!pointsFormRef.value || !selectedMember.value) return

  try {
    // 表单验证
    await pointsFormRef.value.validate()

    const actualPoints = pointsForm.points * pointsForm.type
    const newPoints = calculateNewPoints()

    // 确认对话框
    const action = pointsForm.type === 1 ? '增加' : '扣减'
    const confirmMessage = `确定要为会员 ${selectedMember.value.name} ${action} ${Math.abs(actualPoints)} 积分吗？\n调整后积分：${newPoints}`

    await ElMessageBox.confirm(confirmMessage, '确认积分调整', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    pointsSubmitting.value = true

    // 调用API
    await adjustPoints({
      memberId: selectedMember.value.id,
      points: actualPoints,
      type: pointsForm.type,
      reason: pointsForm.reason,
      remark: pointsForm.remark,
      expireTime: pointsForm.type === 1 ? pointsForm.expireTime : undefined
    })

    // 更新本地数据
    const index = memberList.value.findIndex(m => m.id === selectedMember.value!.id)
    if (index !== -1) {
      memberList.value[index].currentPoints = newPoints
    }

    ElMessage.success('积分调整成功')
    pointsAdjustmentVisible.value = false

  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '积分调整失败')
    }
  } finally {
    pointsSubmitting.value = false
  }
}

// 查看积分记录
const pointsRecordsVisible = ref(false)
const pointsRecords = ref<any[]>([])
const pointsRecordsLoading = ref(false)
const pointsRecordsMember = ref<Member | null>(null)

const handleViewPointsRecords = async (member: Member) => {
  pointsRecordsMember.value = member
  pointsRecordsVisible.value = true
  pointsRecordsLoading.value = true
  pointsRecords.value = []
  try {
    const res = await request.get(`/admin/points/records/${member.id}`)
    pointsRecords.value = (res as any)?.data || []
  } catch { pointsRecords.value = [] }
  finally { pointsRecordsLoading.value = false }
}

// 右键菜单处理
const setupContextMenu = () => {
  document.addEventListener('click', () => {
    contextMenuVisible.value = false
  })

  document.addEventListener('contextmenu', (e) => {
    // 检查是否在表格行上右键
    const target = e.target as HTMLElement
    const row = target.closest('.el-table__row')
    if (row) {
      e.preventDefault()
      const index = parseInt(row.getAttribute('data-row-index') || '0')
      const member = memberList.value[index]
      if (member) {
        contextMenuTarget.value = member
        contextMenuPosition.x = e.clientX
        contextMenuPosition.y = e.clientY
        contextMenuVisible.value = true
      }
    }
  })
}

// 生命周期
onMounted(async () => {
  await fetchStoreList() // 先获取门店列表
  fetchMemberList() // 再获取会员列表
  setupContextMenu()
})

onUnmounted(() => {
  document.removeEventListener('click', () => {
    contextMenuVisible.value = false
  })
  document.removeEventListener('contextmenu', () => {})
})
</script>

<style scoped>
.member-management {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f0f2f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 动态背景图形 */
.background-shapes {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  top: -10%;
  right: -5%;
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #a0c4ff 0%, #c2e9fb 100%);
  animation-delay: 0s;
}

.shape-2 {
  bottom: -10%;
  left: -5%;
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #ffc3a0 0%, #ffafbd 100%);
  animation-delay: -5s;
}

.shape-3 {
  top: 40%;
  left: 40%;
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%);
  animation-delay: -10s;
  opacity: 0.4;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -50px) rotate(10deg); }
  66% { transform: translate(-20px, 20px) rotate(-5deg); }
}

/* 页面主体 */
.page-content {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

/* 统计概览 */
.overview-section {
  margin-bottom: 24px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.overview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, var(--card-color) 0%, var(--card-color-light) 100%);
}

.overview-card.total {
  --card-color: #409eff;
  --card-color-light: #79bbff;
}

.overview-card.active {
  --card-color: #67c23a;
  --card-color-light: #95d475;
}

.overview-card.new {
  --card-color: #e6a23c;
  --card-color-light: #ebb563;
}

.overview-card.revenue {
  --card-color: #f56c6c;
  --card-color-light: #f78989;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--card-color) 0%, var(--card-color-light) 100%);
  color: white;
  font-size: 24px;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 14px;
  font-weight: 500;
}

.card-trend.up {
  color: #67c23a;
}

.card-trend.down {
  color: #f56c6c;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 20px;
}

.filter-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.filter-content {
  padding: 0;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px 24px;
  border-bottom: 1px solid #f0f2f5;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.title-icon {
  color: #409eff;
  font-size: 16px;
}

.filter-count {
  margin-left: 8px;
  font-weight: 500;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 16px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  border-radius: 6px;
  color: #303133 !important;
}

.action-btn:deep(.el-button__text) {
  color: #303133 !important;
  font-weight: 500 !important;
}

.action-btn.el-button--danger:hover {
  color: #f56c6c !important;
  background-color: rgba(245, 108, 108, 0.1) !important;
}

.action-btn.el-button--primary:hover {
  color: #409eff !important;
  background-color: rgba(64, 158, 255, 0.1) !important;
}

/* 基础筛选 */
.basic-filters {
  padding: 20px 24px 16px 24px;
}

.filter-item {
  margin-bottom: 12px;
}

.search-input {
  border-radius: 8px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.search-icon {
  color: #909399;
  font-size: 14px;
}

.filter-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.search-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.reset-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
}

/* 高级筛选 */
.advanced-filters {
  padding: 0 24px 20px 24px;
}

.filter-divider {
  margin: 16px 0 20px 0;
}

.filter-divider :deep(.el-divider__text) {
  background-color: #f8f9fa;
  padding: 8px 16px;
  border-radius: 6px;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #e4e7ed;
}

.filter-divider :deep(.el-divider__text .el-icon) {
  margin-right: 6px;
  color: #409eff;
}

.filter-label {
  display: block;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 8px;
}

.basic-filters {
  margin-bottom: 16px;
}

.advanced-filters {
  padding-top: 16px;
}

/* 快速筛选标签 */
.quick-filters {
  margin-bottom: 20px;
  padding: 16px 0;
}

.filter-tags {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tags-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.filter-tag {
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;
}

.filter-tag:hover {
  transform: translateY(-1px);
}

/* 修复选中标签的文字颜色和背景 */
.filter-tag.el-tag--primary.el-tag--dark {
  color: #409eff !important;
  background-color: #ecf5ff !important;
  border-color: #b3d8ff !important;
}

.filter-tag.el-tag--primary.el-tag--dark .el-tag__content {
  color: #409eff !important;
}

.filter-tag.el-tag--primary.el-tag--dark:hover {
  background-color: #d9ecff !important;
  border-color: #a0cfff !important;
}

/* 确保所有选中状态都有合适的背景色 */
.filter-tag.el-tag--primary {
  background-color: #ecf5ff !important;
  border-color: #b3d8ff !important;
}

.filter-tag.el-tag--primary .el-tag__content {
  color: #409eff !important;
}

/* ========================================
   现代化表格样式 - 完全重构版本
   ======================================== */

/* 表格容器 */
.modern-table-container {
  background: #ffffff;
  border-radius: 16px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  overflow: hidden;
  margin-bottom: 24px;
}

/* 表格头部控制面板 */
.table-header-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.8) 0%,
    rgba(241, 245, 249, 0.9) 100%);
  border-bottom: 1px solid #e5e7eb;
  backdrop-filter: blur(10px);
  position: relative;
}

.table-header-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(59, 130, 246, 0.3) 30%,
    rgba(59, 130, 246, 0.5) 50%,
    rgba(59, 130, 246, 0.3) 70%,
    transparent 100%);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 批量选择指示器 */
.batch-selection-indicator {
  animation: slideInUp 0.3s ease-out;
}

.selection-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  color: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
}

.selection-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
}

.selection-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.selection-count {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
}

.selection-text {
  font-size: 12px;
  opacity: 0.9;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.clear-btn {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  border-radius: 8px !important;
  font-size: 12px !important;
  padding: 6px 10px !important;
  transition: all 0.3s ease !important;
}

.clear-btn:hover {
  background: rgba(255, 255, 255, 0.25) !important;
}

/* 批量操作工具组 */
.batch-tools {
  animation: slideInRight 0.3s ease-out;
}

.tool-group {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #6b7280;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.tool-btn:hover {
  background: #3b82f6;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
}

.tool-btn.danger:hover {
  background: #ef4444;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.25);
}

.tool-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.4s ease;
}

.tool-btn:hover::before {
  left: 100%;
}

.tool-divider {
  width: 1px;
  height: 20px;
  background: #e5e7eb;
  margin: 0 4px;
}

/* 主要操作按钮组 */
.main-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  white-space: nowrap;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn.primary {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
}

.action-btn.primary:hover {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.8);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.2);
  backdrop-filter: blur(10px);
}

.action-btn.secondary:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
}

/* 现代化表格包装器 */
.modern-table-wrapper {
  position: relative;
  min-height: 400px;
}

/* 表格布局容器 */
.table-layout {
  display: flex;
  position: relative;
  background: white;
  border-radius: 0;
  align-items: stretch;
  min-height: 400px; /* 设置最小高度 */
}

/* 主表格区域 */
.table-main-area {
  flex: 1;
  overflow: hidden;
  min-width: 0; /* 允许flex子项收缩 */
  display: flex;
  flex-direction: column;
}

.table-scroll-container {
  overflow-x: auto;
  overflow-y: hidden;
  position: relative;
  /* 添加滑动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(59, 130, 246, 0.3) transparent;
}

/* Webkit 滑动条样式 */
.table-scroll-container::-webkit-scrollbar {
  height: 8px;
}

.table-scroll-container::-webkit-scrollbar-track {
  background: rgba(248, 250, 252, 0.5);
  border-radius: 4px;
}

.table-scroll-container::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.3);
  border-radius: 4px;
  transition: background 0.2s ease;
}

.table-scroll-container::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.5);
}

.modern-table {
  width: 100%;
  min-width: 1200px; /* 减少最小宽度，因为操作列已独立 */
  border-collapse: collapse;
  background: white;
  font-size: 14px;
}

/* 操作列区域 */
.table-actions-area {
  width: 180px;
  flex-shrink: 0;
  position: relative;
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.95) 0%,
    rgba(241, 245, 249, 1) 100%);
  backdrop-filter: blur(16px);
  box-shadow:
    -2px 0 20px rgba(0, 0, 0, 0.06),
    inset 1px 0 0 rgba(255, 255, 255, 0.9);
  border-left: 1px solid rgba(229, 231, 235, 0.4);
  display: flex;
  flex-direction: column;
  height: 100%; /* 确保操作列占满整个表格高度 */
}

/* 操作列表头 */
.actions-header {
  height: 44px; /* 与主表头保持一致 */
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.9) 0%,
    rgba(241, 245, 249, 0.95) 100%);
  backdrop-filter: blur(20px);
  border-bottom: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  position: sticky;
  top: 0;
  z-index: 10;
  margin-top: 0;
}

.actions-header-cell {
  padding: 16px 12px;
  text-align: center;
  font-weight: 600;
  color: #374151;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  user-select: none;
  vertical-align: middle;
}

/* 操作列主体 */
.actions-body {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1; /* 使用flex: 1来填充剩余空间 */
  display: flex;
  flex-direction: column;
}

.actions-body::-webkit-scrollbar {
  width: 6px;
}

.actions-body::-webkit-scrollbar-track {
  background: rgba(248, 250, 252, 0.3);
}

.actions-body::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.2);
  border-radius: 3px;
}

.actions-body::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.4);
}

/* 操作列行 */
.actions-row {
  height: 72px; /* 与主表格行高一致 */
  min-height: 72px; /* 确保最小高度 */
  max-height: 72px; /* 限制最大高度 */
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center; /* 改为center确保垂直居中 */
  justify-content: center;
  position: relative;
  flex-shrink: 0; /* 防止压缩 */
}

/* 悬停时操作行效果 */
.actions-row:hover {
  background: linear-gradient(135deg,
    rgba(248, 249, 250, 0.98) 0%,
    rgba(246, 248, 250, 1) 100%);
  border-left: 2px solid #3b82f6;
}

/* 选中时操作行效果 */
.actions-row.selected {
  background: linear-gradient(135deg,
    rgba(239, 246, 255, 0.95) 0%,
    rgba(219, 234, 254, 1) 100%);
  border-left: 3px solid #3b82f6;
}

.actions-cell {
  width: 100%;
  height: 100%;
  position: relative;
  padding: 0 !important;
  margin: 0 !important;
  box-sizing: border-box;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-height: 56px; /* 确保与表格行高一致 */
  height: 56px; /* 固定高度与表格行保持一致 */
  flex: 1; /* 让cell占满父容器高度 */
}

/* 表格头部 */
.table-header {
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.9) 0%,
    rgba(241, 245, 249, 0.95) 100%);
  backdrop-filter: blur(20px);
  border-bottom: 2px solid #e5e7eb;
}

.header-row {
  height: 44px; /* 减少表头行高以匹配新的padding */
}

.header-cell {
  padding: 8px 12px; /* 减少上下padding与数据行保持一致 */
  text-align: center;
  font-weight: 600;
  color: #374151;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: none;
  white-space: nowrap;
  position: relative;
  user-select: none;
  vertical-align: middle;
}

.header-cell.selection-cell {
  width: 50px;
  text-align: center;
}

.header-cell.name-cell {
  width: 180px;
  min-width: 180px;
  text-align: center;
}

.header-cell.phone-cell {
  width: 140px;
  min-width: 140px;
  text-align: center;
}

.header-cell.number-cell {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

.header-cell.level-cell {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

.header-cell.points-cell {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

.header-cell.consumption-cell {
  width: 130px;
  min-width: 130px;
  text-align: center;
}

.header-cell.store-cell {
  width: 150px;
  min-width: 150px;
  text-align: center;
}

.header-cell.time-cell {
  width: 140px;
  min-width: 140px;
  text-align: center;
}



.header-cell.consumption-time-cell {
  width: 140px;
  min-width: 140px;
}

.header-cell.status-cell {
  width: 100px;
  min-width: 100px;
  text-align: center;
}


.header-cell.sortable {
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-cell.sortable:hover {
  background: rgba(59, 130, 246, 0.05);
  color: #3b82f6;
}

.header-cell.sortable:hover::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #3b82f6;
  border-radius: 0 2px 2px 0;
}

.cell-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.sort-icons {
  display: flex;
  flex-direction: column;
  gap: 1px;
  opacity: 0.3;
  transition: opacity 0.2s ease;
}

.sort-icons.active {
  opacity: 1;
}

.sort-up,
.sort-down {
  font-size: 10px;
  color: #9ca3af;
  transition: all 0.2s ease;
}

.sort-up.active,
.sort-down.active {
  color: #3b82f6;
  transform: scale(1.2);
}

/* 表格内容 */
.table-body {
  background: white;
}

.body-row {
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.2s ease;
  height: 72px; /* 恢复原始行高72px */
}

.body-row:hover {
  background: rgba(59, 130, 246, 0.02);
}

.body-row.selected {
  background: rgba(59, 130, 246, 0.05);
}

.body-row.selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #3b82f6;
}

.body-cell {
  padding: 8px 12px; /* 减少上下padding，保持左右padding */
  border: none;
  vertical-align: middle;
  position: relative;
  text-align: center;
}

.body-cell.selection-cell {
  width: 50px;
  text-align: center;
}

.body-cell.name-cell {
  width: 180px;
  min-width: 180px;
  text-align: center;
}

.body-cell.phone-cell {
  width: 140px;
  min-width: 140px;
  text-align: center;
}

.body-cell.number-cell {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

.body-cell.level-cell {
  width: 120px;
  min-width: 120px;
  text-align: center;
  vertical-align: middle;
  padding: 16px 12px;
}

.body-cell.points-cell {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

.body-cell.consumption-cell {
  width: 130px;
  min-width: 130px;
  text-align: center;
}

.body-cell.store-cell {
  width: 150px;
  min-width: 150px;
  text-align: center;
}

.body-cell.time-cell {
  width: 140px;
  min-width: 140px;
  text-align: center;
}

.body-cell.consumption-time-cell {
  width: 140px;
  min-width: 140px;
  text-align: center;
}

.body-cell.status-cell {
  width: 100px;
  min-width: 100px;
  text-align: center;
}


/* 姓名单元格样式 */
.member-name-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 0;
}

.member-avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  border: 2px solid white;
}

.member-avatar-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text-small {
  font-size: 14px;
  font-weight: 700;
  color: #6b7280;
}

.name-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-info .name-text {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
  text-align: center;
}

.vip-badge-small {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border-radius: 4px;
  font-size: 9px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.2px;
  box-shadow: 0 1px 2px rgba(239, 68, 68, 0.3);
  flex-shrink: 0; /* 防止徽章被压缩 */
}

/* 电话号码单元格 */
.phone-number {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', monospace;
  font-size: 13px;
  color: #3b82f6;
  font-weight: 500;
  padding: 6px 10px;
  background: rgba(59, 130, 246, 0.08);
  border-radius: 6px;
  border: 1px solid rgba(59, 130, 246, 0.15);
  display: inline-block;
  text-align: center;
}

/* 会员号单元格 */
.member-number {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', monospace;
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  padding: 6px 10px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  display: inline-block;
  text-align: center;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  min-height: 24px;
  line-height: 1;
}

.level-badge.level-1 {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.level-badge.level-2 {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border-color: #93c5fd;
}

.level-badge.level-3 {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  border-color: #fcd34d;
}

.level-badge.level-4 {
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  color: #9f1239;
  border-color: #f9a8d4;
}

/* 数值显示 */
.points-value,
.consumption-value {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.number {
  font-size: 16px;
  font-weight: 700;
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  letter-spacing: 0.3px;
}

.points-value .number {
  color: #059669;
}

.consumption-value .number {
  color: #d97706;
}

.currency,
.unit {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}

/* 门店信息 */
.store-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #f3f4f6;
  font-size: 12px;
  color: #6b7280;
}

.store-icon {
  color: #3b82f6;
  font-size: 14px;
  flex-shrink: 0;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date,
.time {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 11px;
  color: #6b7280;
}

.date {
  font-weight: 500;
}

.time {
  color: #9ca3af;
}

.no-data {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
  padding: 2px 6px; /* 进一步减少内边距 */
  background: transparent; /* 移除背景色，减少视觉重量 */
  border-radius: 2px; /* 相应减少圆角 */
  border: 1px dashed #e5e7eb;
  display: inline-block; /* 确保不会撑满整个单元格 */
}

/* 状态指示器 */
.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.status-indicator.active {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.status-indicator.frozen {
  background: #fff7ed;
  color: #ea580c;
  border: 1px solid #fed7aa;
}

.status-indicator.disabled {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  z-index: 1;
  height: 56px; /* 设置确切高度，与行高一致 */
  flex: none; /* 防止按钮组被拉伸 */
  padding: 0; /* 移除额外padding */
}

.action-btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(209, 213, 219, 0.8);
  background: rgba(255, 255, 255, 0.9);
  color: #374151;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.06);
  font-size: 14px;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(8px);
}

.action-btn-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.action-btn-icon:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  color: white;
  border-color: transparent;
  box-shadow:
    0 4px 16px rgba(59, 130, 246, 0.3),
    0 2px 8px rgba(59, 130, 246, 0.2);
  transform: translateY(-2px) scale(1.05);
}

.action-btn-icon:hover::before {
  left: 100%;
}

.action-btn-icon.more:hover {
  background: linear-gradient(135deg, #6b7280 0%, #9ca3af 100%);
  box-shadow:
    0 4px 16px rgba(107, 114, 128, 0.3),
    0 2px 8px rgba(107, 114, 128, 0.2);
}

/* 操作按钮活跃状态 */
.action-btn-icon:active {
  transform: translateY(0) scale(0.98);
  transition: transform 0.1s ease;
}

/* 现代化分页组件 */
.modern-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.9) 0%,
    rgba(241, 245, 249, 0.95) 100%);
  border-top: 1px solid #e5e7eb;
  backdrop-filter: blur(10px);
  position: relative;
}

.modern-pagination::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 2px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(59, 130, 246, 0.4) 20%,
    rgba(59, 130, 246, 0.6) 50%,
    rgba(59, 130, 246, 0.4) 80%,
    transparent 100%);
}

.pagination-info {
  display: flex;
  align-items: center;
}

.total-text {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.page-navigation {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid #d1d5db;
  background: white;
  color: #6b7280;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-btn:hover:not(:disabled) {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #d1d5db;
  background: white;
  color: #374151;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
  color: #3b82f6;
}

.page-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.25);
}

.page-jumper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.jump-input {
  width: 50px;
}

/* 动画效果 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modern-table-container {
    margin: 0 -12px;
    border-radius: 0;
  }

  .table-header-panel {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
    align-items: stretch;
  }

  .header-right {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .main-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .modern-pagination {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 12px;
    align-items: center;
  }
}

/* 表格主体样式 - 全新重构设计 */
.table-card :deep(.el-table) {
  border-radius: 0;
  border: none;
  background: transparent;
  font-size: 14px;
}

/* 表格头部 - 现代化设计 */
.table-card :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.8) 0%,
    rgba(241, 245, 249, 0.9) 100%);
  backdrop-filter: blur(20px);
  border-bottom: 2px solid rgba(64, 158, 255, 0.08);
}

.table-card :deep(.el-table th) {
  background: transparent !important;
  border: none !important;
  color: #374151 !important;
  font-weight: 700 !important;
  font-size: 13px !important;
  padding: 18px 16px !important;
  text-align: center !important;
  position: relative;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table-card :deep(.el-table th::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  transition: width 0.3s ease;
}

.table-card :deep(.el-table th:hover::after) {
  width: 80%;
}

/* 优化表格标题和排序图标 - 紧贴布局 */
.table-card :deep(.el-table th .cell) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 4px !important;
  white-space: nowrap !important;
  font-weight: inherit !important;
  color: inherit !important;
}

.table-card :deep(.el-table th .caret-wrapper) {
  margin-left: 2px !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 1px !important;
}

.table-card :deep(.el-table th .sort-caret) {
  border: none !important;
  width: 8px !important;
  height: 4px !important;
  background: linear-gradient(135deg, #c0c4cc 0%, #dcdfe6 100%) !important;
  clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
  transition: all 0.3s ease !important;
}

.table-card :deep(.el-table th .ascending .sort-caret:first-child),
.table-card :deep(.el-table th .descending .sort-caret:last-child) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  transform: scale(1.2);
}

/* 表格主体行 - 极简无边框设计 */
.table-card :deep(.el-table__body-wrapper) {
  background: rgba(255, 255, 255, 0.6);
}

.table-card :deep(.el-table td) {
  border: none !important;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8) !important;
  padding: 16px !important;
  vertical-align: middle !important;
  transition: all 0.3s ease !important;
  position: relative;
  background: rgba(255, 255, 255, 0.4) !important;
}

.table-card :deep(.el-table td::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 60%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  transition: width 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.table-card :deep(.el-table tbody tr:hover > td) {
  background: rgba(64, 158, 255, 0.02) !important;
  backdrop-filter: blur(10px) !important;
}

.table-card :deep(.el-table tbody tr:hover > td::before) {
  width: 3px;
}

.table-card :deep(.el-table tbody tr > td:last-child::before) {
  display: none;
}

/* 彻底移除所有边框线 */
.table-card :deep(.el-table__inner-wrapper),
.table-card :deep(.el-table__body-wrapper),
.table-card :deep(.el-table__cell),
.table-card :deep(.el-table--border),
.table-card :deep(.el-table--border::after),
.table-card :deep(.el-table--border::before),
.table-card :deep(.el-table__inner-wrapper::after),
.table-card :deep(.el-table__inner-wrapper::before),
.table-card :deep(.el-table::before),
.table-card :deep(.el-table::after) {
  border: none !important;
  display: block !important;
}

.table-card :deep(.el-table .cell),
.table-card :deep(.el-table .el-table__cell) {
  border: none !important;
}

/* 行高度优化 - 单行显示 */
.table-card :deep(.el-table tbody tr) {
  height: 72px !important;
  transition: all 0.3s ease;
}

.table-card :deep(.el-table tbody tr:hover) {
  transform: translateX(2px);
  background: rgba(64, 158, 255, 0.02) !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

/* 会员信息显示 - 现代化设计 */
.member-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 2px 0;
  position: relative;
}

.member-avatar {
  flex-shrink: 0;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1);
  border: 3px solid rgba(255, 255, 255, 0.9);
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  transition: all 0.3s ease;
}

.member-avatar:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow:
    0 6px 20px rgba(64, 158, 255, 0.3),
    0 4px 8px rgba(0, 0, 0, 0.15);
}

.member-details {
  flex: 1;
  min-width: 0;
  position: relative;
}

.member-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
  font-size: 15px;
  letter-spacing: 0.2px;
}

.member-name :deep(.el-tag) {
  font-size: 10px;
  padding: 3px 8px;
  height: 20px;
  line-height: 14px;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
  animation: vipGlow 2s ease-in-out infinite;
}

@keyframes vipGlow {
  0%, 100% { box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3); }
  50% { box-shadow: 0 2px 8px rgba(239, 68, 68, 0.6); }
}

.member-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #6b7280;
  align-items: center;
}

.member-phone {
  color: #374151;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 11px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(37, 99, 235, 0.12) 100%);
  padding: 3px 8px;
  border-radius: 8px;
  border: 1px solid rgba(59, 130, 246, 0.1);
  font-weight: 500;
}

.member-no {
  color: #6b7280;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 11px;
  background: linear-gradient(135deg, rgba(107, 114, 128, 0.08) 0%, rgba(75, 85, 99, 0.12) 100%);
  padding: 3px 8px;
  border-radius: 8px;
  border: 1px solid rgba(107, 114, 128, 0.1);
  font-weight: 500;
}

/* 数据显示组件 - 全新现代化设计 */
.points-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg,
    rgba(16, 185, 129, 0.06) 0%,
    rgba(59, 130, 246, 0.08) 100%);
  border-radius: 12px;
  border: 1px solid rgba(16, 185, 129, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.points-display::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.points-display:hover::before {
  left: 100%;
}

.points-display:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.points-value {
  font-weight: 800;
  color: #059669;
  font-size: 16px;
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  letter-spacing: 0.5px;
}

/* 金额显示 - 金色主题 */
.consumption-amount {
  font-weight: 800;
  color: #d97706;
  font-size: 16px;
  background: linear-gradient(135deg,
    rgba(245, 158, 11, 0.08) 0%,
    rgba(239, 68, 68, 0.06) 100%);
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid rgba(245, 158, 11, 0.1);
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  position: relative;
}

.consumption-amount::before {
  content: '¥';
  font-size: 12px;
  color: #92400e;
  margin-right: 2px;
  font-weight: 600;
}

.consumption-amount:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.2);
}

/* 门店信息 - 地点主题 */
.store-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: linear-gradient(135deg,
    rgba(59, 130, 246, 0.04) 0%,
    rgba(99, 102, 241, 0.06) 100%);
  border-radius: 10px;
  border: 1px solid rgba(59, 130, 246, 0.08);
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.store-info:hover {
  background: linear-gradient(135deg,
    rgba(59, 130, 246, 0.08) 0%,
    rgba(99, 102, 241, 0.12) 100%);
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.store-icon {
  color: #3b82f6;
  font-size: 14px;
  flex-shrink: 0;
}

.store-name {
  font-size: 12px;
  color: #475569;
  flex: 1;
  font-weight: 600;
  line-height: 1.3;
  letter-spacing: 0.2px;
}

.store-name.self-register {
  color: #059669;
  font-weight: 700;
  background: linear-gradient(135deg,
    rgba(16, 185, 129, 0.08) 0%,
    rgba(5, 150, 105, 0.12) 100%);
  padding: 3px 8px;
  border-radius: 8px;
  border: 1px solid rgba(16, 185, 129, 0.1);
}

/* 时间显示 - 时钟主题 */
.time-display {
  font-size: 11px;
  padding: 6px 10px;
  background: linear-gradient(135deg,
    rgba(107, 114, 128, 0.04) 0%,
    rgba(75, 85, 99, 0.06) 100%);
  border-radius: 10px;
  border: 1px solid rgba(107, 114, 128, 0.08);
  line-height: 1.4;
  white-space: nowrap;
  display: inline-block;
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  color: #6b7280;
  font-weight: 500;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.time-display:hover {
  background: linear-gradient(135deg,
    rgba(107, 114, 128, 0.08) 0%,
    rgba(75, 85, 99, 0.12) 100%);
  transform: translateY(-1px);
}

.last-consumption {
  font-size: 11px;
  font-weight: 500;
}

.no-consumption {
  font-size: 11px;
  color: #9ca3af;
  font-style: italic;
  padding: 8px 12px;
  background: linear-gradient(135deg,
    rgba(156, 163, 175, 0.04) 0%,
    rgba(107, 114, 128, 0.06) 100%);
  border-radius: 10px;
  border: 1px dashed rgba(156, 163, 175, 0.2);
  white-space: nowrap;
  display: inline-block;
  font-weight: 400;
  letter-spacing: 0.3px;
}

/* 分页组件 - 现代化重构 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 0;
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.8) 0%,
    rgba(241, 245, 249, 0.9) 100%);
  border-top: 1px solid rgba(64, 158, 255, 0.06);
  position: relative;
  backdrop-filter: blur(20px);
}

.pagination-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 2px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(64, 158, 255, 0.6) 20%,
    rgba(64, 158, 255, 0.8) 50%,
    rgba(64, 158, 255, 0.6) 80%,
    transparent 100%);
  border-radius: 1px;
}

.pagination-wrapper :deep(.el-pagination) {
  background: rgba(255, 255, 255, 0.6);
  padding: 12px 20px;
  border-radius: 16px;
  border: 1px solid rgba(64, 158, 255, 0.08);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper :deep(.el-pagination .el-pager li) {
  background: transparent;
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(64, 158, 255, 0.1);
  font-weight: 500;
  font-size: 13px;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-wrapper :deep(.el-pagination .el-pager li:hover) {
  background: linear-gradient(135deg,
    rgba(64, 158, 255, 0.08) 0%,
    rgba(96, 165, 250, 0.12) 100%);
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.pagination-wrapper :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border-color: transparent;
  font-weight: 700;
  transform: translateY(-2px) scale(1.1);
  box-shadow:
    0 6px 20px rgba(64, 158, 255, 0.4),
    0 2px 8px rgba(64, 158, 255, 0.2);
}

.pagination-wrapper :deep(.el-pagination .btn-prev),
.pagination-wrapper :deep(.el-pagination .btn-next) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.1);
  transition: all 0.3s ease;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.pagination-wrapper :deep(.el-pagination .btn-prev:hover),
.pagination-wrapper :deep(.el-pagination .btn-next:hover) {
  background: linear-gradient(135deg,
    rgba(64, 158, 255, 0.08) 0%,
    rgba(96, 165, 250, 0.12) 100%);
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 右键菜单 - 现代化重构 */
.context-menu {
  position: fixed;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(248, 250, 252, 0.98) 100%);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(64, 158, 255, 0.1);
  backdrop-filter: blur(20px);
  z-index: 1000;
  min-width: 200px;
  padding: 12px;
  animation: contextMenuFadeIn 0.2s ease-out;
}

@keyframes contextMenuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.context-menu-header {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.08);
  border-radius: 8px;
  background: linear-gradient(135deg,
    rgba(64, 158, 255, 0.02) 0%,
    rgba(96, 165, 250, 0.04) 100%);
  margin-bottom: 8px;
}

.member-info-mini {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-name-mini {
  font-weight: 700;
  color: #1f2937;
  font-size: 14px;
  letter-spacing: 0.2px;
}

.context-menu-items {
  padding: 4px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  cursor: pointer;
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  transform: scaleY(0);
  transition: transform 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.menu-item:hover {
  background: linear-gradient(135deg,
    rgba(64, 158, 255, 0.06) 0%,
    rgba(96, 165, 250, 0.1) 100%);
  color: #409eff;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.menu-item:hover::before {
  transform: scaleY(1);
}

.menu-item:active {
  transform: translateX(2px) scale(0.98);
}

/* 空状态优化 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.8) 0%,
    rgba(241, 245, 249, 0.9) 100%);
  border-radius: 16px;
  margin: 20px 0;
  border: 1px solid rgba(64, 158, 255, 0.06);
}

.empty-state-image {
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  opacity: 0.6;
  filter: grayscale(20%);
}

.empty-state-text {
  font-size: 16px;
  color: #6b7280;
  margin-bottom: 8px;
  font-weight: 500;
}

.empty-state-subtext {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.5;
}

/* 加载状态优化 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  border-radius: 16px;
}

.loading-content {
  text-align: center;
  color: #6b7280;
  font-weight: 500;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .table-section {
    margin: 0 -12px;
    border-radius: 0;
  }

  .table-toolbar {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: space-between;
  }

  .table-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .pagination-wrapper {
    padding: 24px 16px;
  }
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .table-section {
    background: linear-gradient(135deg,
      rgba(31, 41, 55, 0.95) 0%,
      rgba(17, 24, 39, 0.98) 100%);
    border-color: rgba(75, 85, 99, 0.3);
  }

  .table-card :deep(.el-table__header-wrapper) {
    background: linear-gradient(135deg,
      rgba(55, 65, 81, 0.8) 0%,
      rgba(75, 85, 99, 0.9) 100%);
  }

  .table-card :deep(.el-table th) {
    color: #f3f4f6;
  }

  .pagination-wrapper {
    background: linear-gradient(135deg,
      rgba(55, 65, 81, 0.8) 0%,
      rgba(75, 85, 99, 0.9) 100%);
  }
}

.menu-item.danger {
  color: #f56c6c;
}

.menu-item.danger:hover {
  background: #fef0f0;
  color: #f56c6c;
}

/* 响应式布局优化 */
@media (max-width: 1200px) {
  .table-toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .table-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .table-section {
    margin: 0 -12px 24px -12px;
    border-radius: 0;
  }

  .table-toolbar {
    padding: 16px;
  }

  .selection-info {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
    gap: 8px;
  }

  .table-actions {
    flex-direction: column;
    gap: 8px;
  }

  .table-actions :deep(.el-button) {
    width: 100%;
  }

  .member-info {
    gap: 10px;
  }

  .member-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .table-card :deep(.el-table th),
  .table-card :deep(.el-table td) {
    padding: 12px 8px;
  }
}

/* 整体容器优化 */
.members-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 24px;
}

/* 头部卡片优化 */
.page-header :deep(.el-card) {
  border-radius: 16px 16px 8px 8px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: none;
  overflow: hidden;
}

.page-header :deep(.el-card__body) {
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  position: relative;
}

.page-header :deep(.el-card__body::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #c471f5 75%, #667eea 100%);
}

/* 筛选卡片优化 */
.filter-section :deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.filter-section :deep(.el-card__body) {
  padding: 0;
}

/* 快速筛选区域优化 */
.quick-filters {
  margin-bottom: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 249, 250, 0.9) 100%);
  border-radius: 12px;
  border: 1px solid rgba(240, 242, 245, 0.8);
  backdrop-filter: blur(10px);
}

/* 加载状态优化 */
.table-card :deep(.el-loading-mask) {
  border-radius: 0 0 16px 16px;
  backdrop-filter: blur(2px);
}

.table-card :deep(.el-loading-spinner) {
  margin-top: -40px;
}

/* 表格空状态优化 */
.table-card :deep(.el-table__empty-block) {
  padding: 60px 20px;
  background: linear-gradient(135deg, #fafbfc 0%, #f0f2f5 100%);
  border-radius: 8px;
  margin: 20px;
  border: 2px dashed #dcdfe6;
}

.table-card :deep(.el-table__empty-text) {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

/* 动画效果 */
@keyframes slideInFromTop {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.table-section {
  animation: slideInFromTop 0.6s ease-out;
}

.filter-section {
  animation: fadeInScale 0.5s ease-out 0.2s both;
}

.quick-filters {
  animation: fadeInScale 0.5s ease-out 0.4s both;
}

/* 滚动条优化 */
.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background: #f5f7fa;
  border-radius: 4px;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: linear-gradient(135deg, #c0c4cc 0%, #909399 100%);
  border-radius: 4px;
  border: 2px solid #f5f7fa;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px;
  }

  .overview-cards {
    grid-template-columns: 1fr;
  }

  
  .filter-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .table-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .toolbar-right {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }

  .basic-filters .el-col {
    margin-bottom: 12px;
  }
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 12px;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.overview-card,
.filter-card,
.table-card {
  animation: fadeIn 0.6s ease-out;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a0a7b3;
}

/* 积分调整对话框样式 */
.points-adjustment-form .member-info {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
}

.points-adjustment-form .member-basic {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.points-adjustment-form .member-basic .member-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.points-adjustment-form .member-basic .member-no {
  font-size: 14px;
  color: #6b7280;
}

.points-adjustment-form .current-points {
  font-size: 14px;
  color: #374151;
}

.points-adjustment-form .current-points .points-value {
  font-size: 18px;
  font-weight: 600;
  color: #3b82f6;
  margin-left: 4px;
}

.points-adjustment-form .form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  padding: 4px 8px;
  background: #f9fafb;
  border-radius: 4px;
  border-left: 3px solid #3b82f6;
}

.points-adjustment-form .el-form-item {
  margin-bottom: 20px;
}

.points-adjustment-form .el-form-item .el-radio-group {
  display: flex;
  gap: 20px;
}

.points-adjustment-form .el-form-item .el-input-number {
  width: 100%;
}

/* 对话框 footer 样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>