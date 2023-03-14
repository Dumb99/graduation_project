import request from '@/utils/request';

export default{
    //hello
    asCheck(input){
        return request({
            url:`/as_check/${input}`,
            method:'get'
        });
    }
};