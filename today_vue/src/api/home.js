import request from '@/utils/request'

// 获取所有社区
export function getCommunities() {
    return request({
        url: '/communities',
        method: 'get'
    })
}

// 根据社区ID获取教室列表
export function getRoomsByCommunity(communityId) {
    return request({
        url: `/rooms/community/${communityId}`,
        method: 'get'
    })
}

// 搜索教室
export function searchRooms(keyword) {
    return request({
        url: `/rooms/search`,
        method: 'get',
        params: { keyword }
    })
}

// 获取教室详情
export function getRoomDetail(roomId) {
    return request({
        url: `/rooms/${roomId}/detail`,
        method: 'get'
    })
}

// 获取可用教室
export function getAvailableRooms(communityId, date, timeSlotId) {
    return request({
        url: `/rooms/available`,
        method: 'get',
        params: { communityId, date, timeSlotId }
    })
}
