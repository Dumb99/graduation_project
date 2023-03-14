import Vue from 'vue'
import Router from 'vue-router'
import { normalizeURL, decode } from 'ufo'
import { interopDefault } from './utils'
import scrollBehavior from './router.scrollBehavior.js'

const _acff310a = () => interopDefault(import('../pages/batch/index.vue' /* webpackChunkName: "pages/batch/index" */))
const _62d43ad5 = () => interopDefault(import('../pages/history/index.vue' /* webpackChunkName: "pages/history/index" */))
const _3a739764 = () => interopDefault(import('../pages/topology/index.vue' /* webpackChunkName: "pages/topology/index" */))
const _9e38d4e0 = () => interopDefault(import('../pages/history/index copy.vue' /* webpackChunkName: "pages/history/index copy" */))
const _68c3c090 = () => interopDefault(import('../pages/index.vue' /* webpackChunkName: "pages/index" */))

const emptyFn = () => {}

Vue.use(Router)

export const routerOptions = {
  mode: 'history',
  base: '/',
  linkActiveClass: 'nuxt-link-active',
  linkExactActiveClass: 'nuxt-link-exact-active',
  scrollBehavior,

  routes: [{
    path: "/batch",
    component: _acff310a,
    name: "batch"
  }, {
    path: "/history",
    component: _62d43ad5,
    name: "history"
  }, {
    path: "/topology",
    component: _3a739764,
    name: "topology"
  }, {
    path: "/history/index%20copy",
    component: _9e38d4e0,
    name: "history-index copy"
  }, {
    path: "/",
    component: _68c3c090,
    name: "index"
  }],

  fallback: false
}

export function createRouter (ssrContext, config) {
  const base = (config._app && config._app.basePath) || routerOptions.base
  const router = new Router({ ...routerOptions, base  })

  // TODO: remove in Nuxt 3
  const originalPush = router.push
  router.push = function push (location, onComplete = emptyFn, onAbort) {
    return originalPush.call(this, location, onComplete, onAbort)
  }

  const resolve = router.resolve.bind(router)
  router.resolve = (to, current, append) => {
    if (typeof to === 'string') {
      to = normalizeURL(to)
    }
    return resolve(to, current, append)
  }

  return router
}
