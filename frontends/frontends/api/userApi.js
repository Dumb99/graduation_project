import request from '@/utils/request';

export default {
    sendCode(mobile) {
        return request({
            url: `/send/${mobile}`,
            method: 'get'
        });
    },
    login(userInfo) {
        return request({
            url: `/login/${userInfo.phone}/${userInfo.code}`,
            method: 'get'
        });
    },
    history(){
        return request({
            url: '/user/getHistory',
            method: 'get'
        });
    }
};