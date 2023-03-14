<template>
  <section class="container">
    <div>
      <el-table
        :data="tableData.slice((currentPage-1)*pageSize,currentPage*pageSize)" 
        align="top"
        border
        style="width: 100%">
        <el-table-column
          prop="id"
          label="序号"
          width="300"
          align="center"/>
        <el-table-column
          prop="from"
          label="来源"
          width="200"
          align="center"/>
        <el-table-column
          prop="asPathStr"
          label="AsPath"
          width="500"
          align="center"/>
        <el-table-column
          prop="success"
          label="检测结果"
          width="150"
          align="center"/>
        <el-table-column
          prop="time"
          label="检测时间"
          width="200"
          align="center"/>
        <el-table-column
          prop="errorMsg"
          label="错误类型"
          width="350"
          align="center"/>
        <el-table-column
          label="错误详情"
          width="130"
          align="center">
          <template slot-scope="scope">
            <el-button 
              :disabled="scope.row.disable" 
              type="text"
              @click="showError(scope.row)"><i class="el-icon-s-data"/>  查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div 
        class="block" 
        style="margin-top:15px;">
        <el-pagination
          :current-page="currentPage" 
          :total="tableData.length" 
          page-size="10" 
          align="center" 
          layout="total, prev, pager, next, jumper" 
          @current-change="handleCurrentChange"/>
      </div>

    </div>
          

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
            v-for="(item, index) in errorList"
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
  </section>
</template>  

<script>

import Vis from 'vis';
import userApi from '@/api/userApi';


export default {
    data() {
        return {
            currentPage: 1, // 当前页码
            total: 10, // 总条数
            pageSize: 10, // 每页的数据条数
            historyResult : [],
            tableData: [],
            errorList:[],
            asPath:[],
            nodes: [],
            edges: [],
            data: {},
            container: null,
            nodesArray:[],
            edgesArray:[],
            checkErrorDialogVisible:false,
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
        this.init();
    },
    methods: {
        init(){
            userApi.history().then(response => {
                this.historyResult = response.data;
                this.tableData = [];
                this.total = this.historyResult.length;
                for(let i = 0 ; i < this.historyResult.length; i++){
                    let item = this.historyResult[i];
                    let path = item.asPath.join(', ');
                    let msg = item.errorList.map((obj,index) => {
                        return obj.errorType;
                    }).join(', ');
                    let dataMeta = {
                        id : item.id,
                        asPathStr: path,
                        success: item.success ? '通过' : '错误',
                        from : item.filename == null ? '单次查询' : '[文件]:'+item.filename,
                        errorMsg: msg,
                        asPath : item.asPath,
                        time : item.time,
                        errorList : item.errorList,
                        disable : item.success,
                    };
                    this.tableData.push(dataMeta);
                };
            });
        },
        //当前页改变时触发 跳转其他页
        handleCurrentChange(val) {
            this.currentPage = val;
        },
        drawTop(asPath, errAs, index){
            this.nodesArray = [];
            this.edgesArray = [];
            this.network = {};
            this.container=null;
            let pre = '';
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
                this.nodesArray.push(node);
                if(i != 0){
                    let edge = {
                        from : pre,
                        to : as,
                    };
                    this.edgesArray.push(edge);
                }
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
        tabClick(tab){
            this.drawTop(this.asPath, this.errorList[tab.index].errAs, tab.index);
            this.network.redraw();
            this.$forceUpdate();
        },
        showError(item){
            this.asPath = item.asPath;
            this.errorList = item.errorList;
            this.checkErrorDialogVisible= true;
            let tab = {
                index : 0,
            };
            this.tabClick(tab);
            this.$forceUpdate();
        }
    }
};
</script>


<style>
.container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.title {
  font-family: "Quicksand", "Source Sans Pro", -apple-system, BlinkMacSystemFont,
    "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; /* 1 */
  display: block;
  font-weight: 300;
  font-size: 100px;
  color: #35495e;
  letter-spacing: 1px;
}

.subtitle {
  font-weight: 300;
  font-size: 42px;
  color: #526488;
  word-spacing: 5px;
  padding-bottom: 15px;
}

.links {
  padding-top: 15px;
}
</style>