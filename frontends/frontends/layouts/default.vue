<template>
  <div class="app-container">
    <div id="main">
      <!-- 公共头 -->
      <myheader/>
      <div class="main-container">
        <el-scrollbar class="page-component__scroll">
          <!-- 内容区域 -->
          <nuxt/>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>
<script>
import '~/assets/css/app.css';
import '~/assets/css/chunk.css';
import '~/assets/css/iconfont.css';
import '~/assets/css/main.css';
import myheader from './myheader';
export default {
    components: {
        myheader
    }
};
</script>