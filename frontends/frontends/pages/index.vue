<template>
  <img 
    style="width: 80%, height:80%" 
    src="~assets/images/bckg2.jpg">
</template>  

<script>
import check from '@/api/check';
import { getUserIP } from '@/api/getIp';

export default {
    data() {
        return {
            ip: ''
        };
    },
    mounted() {
    },
    methods: {
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

 #logo{
    background: url("../assets/images/bckg.jpeg");
    background-size: 100% 100%;
    height: 100%;
  }

</style>

