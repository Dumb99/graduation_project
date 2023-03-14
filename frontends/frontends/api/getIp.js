function getUserIP(func){
    if(typeof window != 'undefined'){
        var RTCPeerConnection = window.RTCPeerConnection ||  window.webkitRTCPeerConnection;
        // window.mozRTCPeerConnection ||
        if (RTCPeerConnection) (()=>{
        
            var rtc = new RTCPeerConnection();
        
            rtc.createDataChannel(''); //创建一个可以发送任意数据的数据通道
        
            rtc.createOffer(( offerDesc )=> { //创建并存储一个sdp数据
        
                rtc.setLocalDescription(offerDesc);
        
            }, (e) => { console.log(e);});
        
            rtc.onicecandidate =(evt) => { //监听candidate事件
        
                if (evt.candidate) {
        
                    console.log('evt:',evt.candidate);
        
                    let ip_rule = /([0-9]{1,3}(\.[0-9]{1,3}){3}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7})/;
        
                    var ip_addr = ip_rule.exec(evt.candidate.candidate);
                    // this.test(ip_addr);//调用方法把ip地址的值传出去
                    func(ip_addr);
                }};
        })();
    }
}
export {
    getUserIP
};