import wx from 'weixin-js-sdk';
import request from '@/utils/request'; // 假设你的axios封装在这里，请根据实际情况修改

export const initWxShare = async (shareData) => {
    // 1. 判断是否在微信环境
    const ua = navigator.userAgent.toLowerCase();
    if (!ua.includes('micromessenger')) {
        console.log('非微信环境，跳过分享配置');
        return;
    }

    try {
        // 2. 获取当前页面 URL (去除 hash 部分)
        // 注意：如果你的路由模式是 hash (带#)，微信签名的 URL 需要是 window.location.href.split('#')[0]
        // 如果是 history 模式，则是 window.location.href
        // 这里假设你是 hash 模式，如果签名失败，请尝试去掉 .split('#')[0]
        const currentUrl = window.location.href.split('#')[0];

        // 3. 请求后端签名接口
        const res = await request.get('/api/wechat/js-sdk-config', {
            params: { url: currentUrl }
        });

        // 根据你后端的返回结构，可能是 res.data 或者直接是 res
        const configData = res.data || res;
        const { appId, timestamp, nonceStr, signature } = configData;

        // 4. 配置 SDK
        wx.config({
            debug: false, // 开发阶段可以设为 true，手机上会弹窗报错信息
            appId: appId,
            timestamp: timestamp,
            nonceStr: nonceStr,
            signature: signature,
            jsApiList: [
                'updateAppMessageShareData', // 分享给朋友
                'updateTimelineShareData'    // 分享到朋友圈
            ]
        });

        // 5. 配置分享内容
        wx.ready(() => {
            const shareConfig = {
                title: shareData.title || '宁德师院活动报名',
                desc: shareData.desc || '精彩活动等你来参加！',
                link: window.location.href, // 分享出去的链接，必须是当前页面的链接
                imgUrl: shareData.imgUrl || 'https://你的域名/logo.png', // 分享图标，必须是绝对路径
                success: function () {
                    console.log('分享设置成功');
                }
            };

            // 分享给朋友 (新版API)
            wx.updateAppMessageShareData(shareConfig);
            // 分享到朋友圈 (新版API)
            wx.updateTimelineShareData(shareConfig);
        });

        wx.error((err) => {
            console.error('微信SDK配置失败:', err);
        });

    } catch (error) {
        console.error('获取微信签名失败:', error);
    }
};
