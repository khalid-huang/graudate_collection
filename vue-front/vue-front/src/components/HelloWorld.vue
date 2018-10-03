<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <router-link to="/file"> multifile </router-link>
  </div>
</template>

<script>
import axios from 'axios';
import {testPostApi, testGetApi, testPostProject, testGetProject, testAddProject, testUpdateProject, testStartProcess} from '../api/api';

export default {
  name: 'HelloWorld',
  data () {
    return {
      msg: 'Welcome to Your Vue.js App'
    }
  },
  mounted (){
    let variables = {
      test: "test"
    }
    let processModelKey = "travel-booking"
    testStartProcess(variables, processModelKey)
      .then( res => {
        console.log(res)
      })
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1, h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
