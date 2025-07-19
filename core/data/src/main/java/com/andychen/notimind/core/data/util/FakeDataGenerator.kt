package com.andychen.notimind.core.data.util

import com.andychen.notimind.core.model.NotificationEntity
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

/**
 * Utility class for generating fake notification data for testing and demonstration purposes.
 */
object FakeDataGenerator {
    
    private val sampleApps = listOf(
        "com.tencent.mm" to "微信",
        "com.sina.weibo" to "微博",
        "com.taobao.taobao" to "淘宝",
        "com.tencent.mobileqq" to "QQ",
        "com.ss.android.ugc.aweme" to "抖音",
        "com.alibaba.android.rimet" to "钉钉",
        "com.netease.cloudmusic" to "网易云音乐",
        "com.baidu.BaiduMap" to "百度地图",
        "com.jingdong.app.mall" to "京东",
        "com.eg.android.AlipayGphone" to "支付宝",
        "com.zhihu.android" to "知乎",
        "com.tencent.news" to "腾讯新闻",
        "com.ximalaya.ting.android" to "喜马拉雅",
        "com.meituan.android.common" to "美团",
        "com.dianping.v1" to "大众点评"
    )
    
    private val newsApps = listOf(
        "com.tencent.news" to "腾讯新闻",
        "com.netease.news" to "网易新闻",
        "com.sina.news" to "新浪新闻",
        "com.sohu.news" to "搜狐新闻"
    )
    
    private val socialApps = listOf(
        "com.tencent.mm" to "微信",
        "com.sina.weibo" to "微博",
        "com.tencent.mobileqq" to "QQ",
        "com.ss.android.ugc.aweme" to "抖音"
    )
    
    private val shoppingApps = listOf(
        "com.taobao.taobao" to "淘宝",
        "com.jingdong.app.mall" to "京东",
        "com.tmall.wireless" to "天猫",
        "com.xunmeng.pinduoduo" to "拼多多"
    )
    
    private val workApps = listOf(
        "com.alibaba.android.rimet" to "钉钉",
        "com.tencent.wework" to "企业微信",
        "com.microsoft.teams" to "Microsoft Teams"
    )
    
    private val newsTitles = listOf(
        "科技前沿：人工智能技术新突破",
        "经济动态：股市今日收盘情况",
        "国际新闻：重要国际会议召开",
        "体育赛事：足球比赛精彩回顾",
        "天气预报：明日天气情况预告",
        "健康资讯：专家建议健康生活方式",
        "教育新闻：教育改革最新政策",
        "文化娱乐：新电影上映预告"
    )
    
    private val newsContents = listOf(
        "最新研究显示，人工智能在医疗诊断领域取得重大进展...",
        "今日股市表现活跃，多个板块出现上涨趋势...",
        "国际领导人就全球气候变化问题进行深入讨论...",
        "昨晚的足球比赛精彩纷呈，双方球员表现出色...",
        "气象部门预测，明日将有小雨，请注意出行安全...",
        "专家提醒，保持规律作息对身体健康非常重要...",
        "教育部发布新政策，旨在提高教育质量...",
        "备受期待的新电影即将上映，预售票房火爆..."
    )
    
    private val socialTitles = listOf(
        "张三",
        "李四",
        "王五",
        "赵六",
        "工作群",
        "家庭群",
        "同学群",
        "朋友圈"
    )
    
    private val socialContents = listOf(
        "今天天气真不错，出去走走吧！",
        "刚看了一部很棒的电影，推荐给大家",
        "周末有空一起聚餐吗？",
        "分享一个有趣的文章给大家看看",
        "工作进展顺利，感谢大家的支持",
        "家里的小猫咪又调皮了",
        "今天学到了新知识，很有收获",
        "晚安，祝大家好梦！"
    )
    
    private val shoppingTitles = listOf(
        "订单状态更新",
        "限时优惠活动",
        "商品推荐",
        "物流信息",
        "支付成功",
        "评价提醒",
        "会员福利",
        "新品上架"
    )
    
    private val shoppingContents = listOf(
        "您的订单已发货，预计明天送达",
        "限时特价商品，错过就没有了！",
        "根据您的浏览记录，为您推荐这些商品",
        "您的包裹正在配送中，请保持手机畅通",
        "支付成功，感谢您的购买",
        "请为您购买的商品进行评价",
        "会员专享优惠券已到账",
        "新品首发，抢先体验"
    )
    
    private val workTitles = listOf(
        "会议通知",
        "项目更新",
        "任务提醒",
        "审批通知",
        "公告发布",
        "考勤提醒",
        "培训通知",
        "系统维护"
    )
    
    private val workContents = listOf(
        "明天上午10点召开项目会议，请准时参加",
        "项目进度已更新，请查看最新状态",
        "您有一个任务即将到期，请及时处理",
        "您的申请已通过审批",
        "公司发布重要公告，请及时查看",
        "请记得打卡签到",
        "下周将举行技能培训，欢迎报名参加",
        "系统将于今晚进行维护，请提前保存工作"
    )
    
    /**
     * Generate fake notifications for the past few days
     */
    fun generateFakeNotifications(count: Int = 50): List<NotificationEntity> {
        val notifications = mutableListOf<NotificationEntity>()
        val now = LocalDateTime.now()
        
        repeat(count) { index ->
            val hoursAgo = Random.nextInt(0, 72) // Past 3 days
            val timestamp = now.minusHours(hoursAgo.toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            val notificationType = Random.nextInt(0, 5)
            val (packageName, appName, title, content) = when (notificationType) {
                0 -> generateNewsNotification()
                1 -> generateSocialNotification()
                2 -> generateShoppingNotification()
                3 -> generateWorkNotification()
                else -> generateRandomNotification()
            }
            
            notifications.add(
                NotificationEntity(
                    id = index.toLong() + 1,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    category = determineCategory(packageName),
                    isRemoved = false
                )
            )
        }
        
        return notifications.sortedByDescending { it.timestamp }
    }
    
    private fun generateNewsNotification(): NotificationData {
        val (packageName, appName) = newsApps.random()
        val title = newsTitles.random()
        val content = newsContents.random()
        return NotificationData(packageName, appName, title, content)
    }
    
    private fun generateSocialNotification(): NotificationData {
        val (packageName, appName) = socialApps.random()
        val title = socialTitles.random()
        val content = socialContents.random()
        return NotificationData(packageName, appName, title, content)
    }
    
    private fun generateShoppingNotification(): NotificationData {
        val (packageName, appName) = shoppingApps.random()
        val title = shoppingTitles.random()
        val content = shoppingContents.random()
        return NotificationData(packageName, appName, title, content)
    }
    
    private fun generateWorkNotification(): NotificationData {
        val (packageName, appName) = workApps.random()
        val title = workTitles.random()
        val content = workContents.random()
        return NotificationData(packageName, appName, title, content)
    }
    
    private fun generateRandomNotification(): NotificationData {
        val (packageName, appName) = sampleApps.random()
        val title = "通知标题 ${Random.nextInt(1, 100)}"
        val content = "这是一条示例通知内容，用于演示应用功能。"
        return NotificationData(packageName, appName, title, content)
    }
    
    private fun determineCategory(packageName: String): String {
        return when {
            packageName.contains("news") || packageName.contains("tencent.news") -> "NEWS"
            packageName.contains("mm") || packageName.contains("qq") || packageName.contains("weibo") -> "PERSONAL_MESSAGE"
            packageName.contains("taobao") || packageName.contains("jingdong") || packageName.contains("tmall") -> "PROMOTION"
            packageName.contains("rimet") || packageName.contains("wework") || packageName.contains("teams") -> "SYSTEM"
            else -> "OTHER"
        }
    }
    
    private data class NotificationData(
        val packageName: String,
        val appName: String,
        val title: String,
        val content: String
    )
}