<template>
  <section class="container">
    <el-tabs 
      tab-position="left" 
      style="height: 95vh;"
      @tab-click="fileTabClick">
      <el-tab-pane
        v-for="(item, index) in batchResult"
        :key="index"
        :label="item.filename">
        <el-table
          :data="tableData"
          border
          style="width: 100%">
          <el-table-column
            prop="id"
            label="序号"
            width="100"
            align="center"/>
          <el-table-column
            prop="asPathStr"
            label="AsPath"
            width="650"
            align="center"/>
          <el-table-column
            prop="success"
            label="检测结果"
            width="200"
            align="center"/>
          <el-table-column
            prop="errorMsg"
            label="错误类型"
            width="550"
            align="center"/>
          <el-table-column
            label="错误详情"
            width="200"
            align="center">
            <template slot-scope="scope">
              <el-button 
                :disabled="scope.row.disable" 
                type="text"
                @click="showError(scope.row)"><i class="el-icon-s-data"/>  查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

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


export default {
    data() {
        return {
            batchResult : [],
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
            this.batchResult = JSON.parse(localStorage.getItem('batchResult'));
            let tab = {
                index : 0,
            };
            this.fileTabClick(tab);
        },
        fileTabClick(tab){
            this.tableData = [];
            let index = 1;
            for(let i = 0 ; i < this.batchResult[tab.index].response.length; i++){
                let item = this.batchResult[tab.index].response[i];
                let path = item.asPath.join(', ');
                let msg = item.errorList.map((obj,index) => {
                    return obj.errorType;
                }).join(', ');
                let dataMeta = {
                    id : index++,
                    asPathStr: path,
                    success: item.success ? '通过' : '错误',
                    errorMsg: msg,
                    disable : item.success,
                    asPath : item.asPath,
                    errorList : item.errorList,
                };
                this.tableData.push(dataMeta);
            };
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
            console.log('container: '+ JSON.stringify(this.container[index]));
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