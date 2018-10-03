import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import Multifile from '@/components/Multifile'


Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/HelloWorld'
    },  
    {
      path: '/HelloWorld',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
      path: '/file',
      name: 'Multifile',
      component: Multifile
    }
  ]
})
