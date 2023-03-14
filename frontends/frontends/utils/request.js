import axios from 'axios';
import cookie from 'js-cookie';
// 创建axios实例
const service = axios.create({
    // baseURL: 'http://118.178.234.115:8080',
    baseURL: 'http://localhost:8080',
    timeout: 15000 // 请求超时时间
});
// http request 拦截器
service.interceptors.request.use(
    config => {
        if(cookie.get('token')){
            config.headers['token'] = cookie.get('token');
        }
        return config;
    },
    err => {
        return Promise.reject(err);
    });
// http response 拦截器
// service.interceptors.response.use(
//     response => {
//         if (response.data.code !== 200) {
//             Message({
//                 message: response.data.message,
//                 type: 'error',
//                 duration: 5 * 1000
//             });
//             return Promise.reject(response.data);
//         } else {
//             return response.data;
//         }
//     },
//     error => {
//         return Promise.reject(error.response);
//     });
export default service;