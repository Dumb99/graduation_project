<template>
  <div class="header-container">
    <div class="wrapper">
      <!-- logo -->
      <div 
        class="left-wrapper v-link selected"
        @click="fristPage">
        <img 
          style="width: 60px" 
          width="60" 
          height="50" 
          src="~assets/images/HIT.jpg">
        <span class="text">路由路径异常检测</span>
      </div>
      <!-- 搜索框 -->
      <div class="search-wrapper">
        <div class="hospital-search animation-show">
          <div 
            style="margin-top: 10px;"
            width="500" >
            <el-input 
              v-model="asInput" 
              style="width:650px"
              prefix-icon="el-icon-search"
              placeholder="Please input as path"
              clearable>
              <span 
                slot="append" 
                class="search-btn v-link highlight clickable selected"
                @click="asCheck(asInput)">检测 </span>
            </el-input>
          </div>
        </div>
      </div> 
      <!-- 右侧 -->
      <div class="right-wrapper">
        <el-dropdown @command="mainFunction">
          <span class="el-dropdown-link">主要功能<i class="el-icon-arrow-down el-icon--right"/></span>
          <el-dropdown-menu 
            slot="dropdown" 
            class="user-name-wrapper">
            <el-dropdown-item command= "batchCheck">批量检测</el-dropdown-item>   
            <el-dropdown-item command="showtop">拓扑展示</el-dropdown-item>
            <!-- <el-dropdown-item command="localInformation">本机信息</el-dropdown-item> -->
          </el-dropdown-menu>
        </el-dropdown>
        <span 
          v-if="name == ''" 
          id="loginDialog" 
          class="v-link clickable" 
          @click="showLogin()">登录/注册</span>
        <el-dropdown 
          v-if="name != ''" 
          @command="loginMenu">
          <span class="el-dropdown-link">
            {{ name }}<i class="el-icon-arrow-down el-icon--right"/>
          </span>
          <el-dropdown-menu 
            slot="dropdown" 
            class="user-name-wrapper">
            <el-dropdown-item command="/history">历史记录</el-dropdown-item>
            <el-dropdown-item 
              command="/logout" 
              divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

      <el-dialog 
        v-if="dialogUserFormVisible" 
        :visible.sync="dialogUserFormVisible" 
        :append-to-body="true" 
        top="200px" 
        width="30%" 
        @close="closeDialog()">
        <!-- 手机登录 #start -->
        <div 
          class="operate-view" >
          <div 
            class="wrapper" 
            style="width: 100%" 
            center>
            <div 
              class="mobile-wrapper" 
              style="position: static;width: 80%">
              <span 
                style="text-align: left;display:inline-block;width:100%;font-weight: 700;color: #333;font-size: 30px;letter-spacing: 1px;"
              >{{ dialogAtrr.labelTips }}</span>
              <el-form>
                <el-form-item>
                  <div style="position: static">
                    <el-input 
                      v-model="dialogAtrr.inputValue" 
                      :placeholder="dialogAtrr.placeholder" 
                      :maxlength="dialogAtrr.maxlength" 
                      class="input v-input">
                      <span 
                        v-if="dialogAtrr.second > 0" 
                        slot="suffix" 
                        class="sendText v-link">{{ dialogAtrr.second }}s </span>
                      <span 
                        v-if="dialogAtrr.second == 0" 
                        slot="suffix" 
                        class="sendText v-link highlight clickable selected" 
                        @click="getCodeFun()">重新发送 </span>
                    </el-input>
                  </div>
                </el-form-item>
              </el-form>
              <div 
                class="send-button v-button" 
                @click="btnClick()"> {{ dialogAtrr.loginBtn }}</div>
            </div>
          </div>
        </div >
      </el-dialog>

      <!--批量查询-->
      <el-dialog
        :visible.sync="batchCheckDialogVisible"
        :before-close="handleClose"
        :append-to-body="true" 
        title="批量检测"
        top="360px"
        width="25%">
        <div 
          class="wrapper" >
          <el-upload
            ref="upload"
            :auto-upload="false"
            :on-success="handle_success"
            :action="batchCheckUrl"
            class="upload-demo">
            <el-button 
              slot="trigger" 
              size="small" 
              type="success">选择文件</el-button>
            <el-button 
              style="margin-left: 10px;" 
              size="small" 
              type="primary" 
              @click="submitUpload">执行检测</el-button>
          </el-upload>
        </div>
      </el-dialog>
      <!--批量查询-->


      <!--单条查询-->
      <el-dialog
        :visible.sync="checkErrorDialogVisible"
        :append-to-body="true" 
        title="异常检测"
        top="300px"
        width="30%">
        <div 
          class="wrapper" >
          <el-tabs 
            tab-position="left" 
            style="height: 300px;"
            @tab-click="tabClick">
            <el-tab-pane
              v-for="(item, index) in checkVo.errorList"
              :key="index"
              :label="item.errorType">
              {{ item.errorMsg }}
              <div 
                id="network_id" 
                class="network" 
                style="height:30vh"/></el-tab-pane>
          </el-tabs>
        </div>
      </el-dialog>
      <!--单条查询-->
    </div>
  </div>
</template>

<script>

import cookie from 'js-cookie';
import Vue from 'vue';
import check from '@/api/check';
import userApi from '@/api/userApi';
import Vis from 'vis';


const defaultDialogAtrr = {
    labelTips: '邮箱地址', // 输入框提示
    inputValue: '', // 输入框绑定对象
    placeholder: '请输入您的邮箱', // 输入框placeholder
    maxlength: 50, // 输入框长度控制

    loginBtn: '获取验证码', // 登录按钮或获取验证码按钮文本
    sending: true,      // 是否可以发送验证码
    second: -1,        // 倒计时间  second>0 : 显示倒计时 second=0 ：重新发送 second=-1 ：什么都不显示
    clearSmsTime: null  // 倒计时定时任务引用 关闭登录层清除定时任务
};

export default {
    data() {
        return {
            userInfo: {
                phone: '',
                code: ''
            },
            batchCheckUrl:'http://localhost:8080/batchCheck/',
            filesnum:0,
            checkVo:{},
            nodes: [],
            edges: [],
            data: {},
            batchResult:[],
            // network:null,
            container: null,
            nodesArray:[],
            edgesArray:[],
            checkErrorDialogVisible:false,
            asInput: null,
            asPath:[],
            dialogUserFormVisible: false,
            // 弹出层相关属性
            dialogAtrr:defaultDialogAtrr,
            name: '', // 用户登录显示的名称
            batchCheckDialogVisible: false,
            options : {
                nodes: {
                    color: {
                        border: '#2B7CE9',
                        // background: '#D2E5FF',
                    },
                    shape: 'circle',
                    size: 20,
                    label: 'test'
                }
            },
        };
    },
    mounted() {
    // 注册全局登录事件对象
        window.loginEvent = new Vue();
        // 监听登录事件
        loginEvent.$on('loginDialogEvent', function () {
            document.getElementById('loginDialog').click();
        });
    // 触发事件，显示登录层：loginEvent.$emit('loginDialogEvent')
    },
    created() {
        this.showInfo();
    },
    methods: {
        tabClick(tab){
            this.drawTop(this.checkVo.asPath, this.checkVo.errorList[tab.index].errAs, tab.index);
            this.network.redraw();
            this.$forceUpdate();
        },
        drawTop(asPath, errAs, index){
            this.nodesArray = [];
            this.edgesArray = [];
            this.network = {};
            this.container=null;
            let pre = '';
            let exit = [];
            for (let i = 0; i < asPath.length; i++) {
                let as = asPath[i];
                let node = {
                    id : as,
                    label : as,
                    color: {background: 'DodgerBlue' }
                };
                if(errAs.indexOf(as) > -1){
                    node.color = {background: 'Salmon' };
                }
                if(exit.indexOf(as) < 0){
                    this.nodesArray.push(node);
                }
                if(i != 0){
                    let edge = {
                        from : pre,
                        to : as,
                    };
                    this.edgesArray.push(edge);
                }
                exit.push(as);
                pre = as;
            }
            let _this = this;
            _this.nodes = new Vis.DataSet(this.nodesArray);
            _this.edges = new Vis.DataSet(this.edgesArray);
            _this.container = document.getElementsByClassName('network');
            _this.data = {
                nodes: _this.nodes,
                edges: _this.edges
            };
            _this.network = new Vis.Network(
                _this.container[index],
                _this.data,
                _this.options
            );
        },
        handle_success(res,file,arr){
            this.filesnum++;
            let result = {
                filename : file.name,
                response : res,
            };
            this.batchResult.push(result);
            localStorage.setItem('batchResult', JSON.stringify(this.batchResult));
            if(arr.length <= this.filesnum){
                window.location.href = '/batch';
            }
        },
        mainFunction(commond) {
            if('batchCheck'==commond){
                this.batchCheckDialogVisible= true;
                this.batchCheckUrl = this.batchCheckUrl + localStorage.getItem('email');
            } else if ('showtop' == commond){
                window.open('http://localhost:8888/topology','_blank');
            } else if('localInformation' == commond){
            }
        },
        submitUpload() {
            this.batchResult =[];
            this.filesnum = 0;
            this.$refs.upload.submit();
        },
        handleClose(done) {
            this.$confirm('Are you sure to close this dialog?')
                .then(_ => {
                    done();
                })
                .catch(_ => {});
        },
        asCheck(asInput) {
            check.asCheck(asInput).then(response => {
                if(response.data.success){
                    this.$alert('As Path 路径无异常', '检测通过', {
                        confirmButtonText: 'OK',
                        type: 'OK'
                    });
                } else {
                    this.checkVo = response.data;
                    this.asPath = response.data.asPath;
                    this.checkErrorDialogVisible= true;
                    this.$forceUpdate();
                }
            }).catch(e => {
                console.log('Error:'+e);
            });
            
        },
        btnClick() {
            // 判断是获取验证码还是登录
            if(this.dialogAtrr.loginBtn == '获取验证码') {
                this.userInfo.phone = this.dialogAtrr.inputValue;

                // 获取验证码
                this.getCodeFun();
            } else {
                // 登录
                this.login();
            }
        },
        fristPage(){
            window.location.href = 'http://localhost:8888';
        },
        showLogin() {
            this.dialogUserFormVisible = true;

            // 初始化登录层相关参数
            this.dialogAtrr = { ...defaultDialogAtrr };
        },
        showInfo() {
            let token = cookie.get('token');
            if (token) {
                this.name = cookie.get('name');
            }
        },
        closeDialog() {
            if(this.clearSmsTime) {
                clearInterval(this.clearSmsTime);
            }
        },
        loginMenu(command) {
            if('/logout' == command) {
                cookie.set('name', '', {domain: 'localhost'});
                cookie.set('token', '', {domain: 'localhost'});

                //跳转页面
                window.location.href = '/';
            } else if ('/history' == command){
                window.location.href = command;
            }
        },
        // 登录
        login() {
            this.userInfo.code = this.dialogAtrr.inputValue;

            if(this.dialogAtrr.loginBtn == '正在提交...') {
                this.$message.error('重复提交');
                return;
            }
            if (this.userInfo.code == '') {
                this.$message.error('验证码必须输入');
                return;
            }
            if (this.userInfo.code.length != 6) {
                this.$message.error('验证码格式不正确');
                return;
            }
            this.dialogAtrr.loginBtn = '正在提交...';
            userApi.login(this.userInfo).then(response => {
                console.log(response.data);
                // 登录成功 设置cookie
                localStorage.setItem('email',response.data.email);
                this.setCookies(response.data.name, response.data.token);
            }).catch(e => {
                this.dialogAtrr.loginBtn = '马上登录';
            });
        },

        setCookies(name, token) {
            cookie.set('token', token, { domain: 'localhost' });
            cookie.set('name', name, { domain: 'localhost' });
            window.location.reload();
        },

        // 获取验证码
        getCodeFun() {
            if(!(this.userInfo.phone).match(/^\w+@\w+\.\w+$/i)){
                this.$message.error('邮箱不正确');
                return;
            }

            // 初始化验证码相关属性
            this.dialogAtrr.inputValue = '';
            this.dialogAtrr.placeholder = '请输入验证码';
            this.dialogAtrr.maxlength = 6;
            this.dialogAtrr.loginBtn = '马上登录';

            // 控制重复发送
            if (!this.dialogAtrr.sending) return;

            // 发送短信验证码
            this.timeDown();
            this.dialogAtrr.sending = false;
            userApi.sendCode(this.userInfo.phone).then(response => {
                this.timeDown();
            }).catch(e => {
                this.$message.error('发送失败，重新发送');
                // 发送失败，回到重新获取验证码界面
                this.showLogin();
            });
        },

        // 倒计时
        timeDown() {
            if(this.clearSmsTime) {
                clearInterval(this.clearSmsTime);
            }
            this.dialogAtrr.second = 120;
            this.dialogAtrr.labelTips = '验证码已发送至: ' + this.userInfo.phone;
            this.clearSmsTime = setInterval(() => {
                --this.dialogAtrr.second;
                if (this.dialogAtrr.second < 1) {
                    clearInterval(this.clearSmsTime);
                    this.dialogAtrr.sending = true;
                    this.dialogAtrr.second = 0;
                }
            }, 1000);
        },
        resetAllNodes() {
            let _this = this;
            _this.nodes.clear();
            _this.edges.clear();
            _this.nodes.add(_this.nodesArray);
            _this.edges.add(_this.edgesArray);
            _this.data = {
                nodes: _this.nodes,
                edges: _this.edges
            };
            //   network是一种用于将包含点和线的网络和网络之间的可视化展示
            _this.network = new Vis.Network(
                _this.container,
                _this.data,
                _this.options
            );
        },
        resetAllNodesStabilize() {
            let _this = this;
            _this.resetAllNodes();
            _this.network.stabilize();
        }

    }

};
</script>