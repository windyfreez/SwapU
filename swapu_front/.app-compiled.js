import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'


export default {
  __name: 'App',
  setup(__props, { expose: __expose }) {
  __expose();

const route = useRoute()
const year = new Date().getFullYear()

// 登录/注册页使用独立的全屏布局,不显示导航与页脚
const showChrome = computed(() => !['/login', '/register'].includes(route.path))

const __returned__ = { route, year, showChrome, computed, get useRoute() { return useRoute }, NavBar }
Object.defineProperty(__returned__, '__isScriptSetup', { enumerable: false, value: true })
return __returned__
}

}
import { resolveComponent as _resolveComponent, openBlock as _openBlock, createBlock as _createBlock, createCommentVNode as _createCommentVNode, createVNode as _createVNode, createElementVNode as _createElementVNode, toDisplayString as _toDisplayString, createElementBlock as _createElementBlock } from "vue"

const _hoisted_1 = { class: "app-shell" }
const _hoisted_2 = { class: "app-main" }
const _hoisted_3 = {
  key: 1,
  class: "app-footer"
}
const _hoisted_4 = { class: "container footer-inner" }

export function render(_ctx, _cache) {
  const _component_NavBar = _resolveComponent("NavBar")
  const _component_router_view = _resolveComponent("router-view")

  return (_openBlock(), _createElementBlock("div", _hoisted_1, [
    (_ctx.showChrome)
      ? (_openBlock(), _createBlock(_component_NavBar, { key: 0 }))
      : _createCommentVNode("v-if", true),
    _createElementVNode("main", _hoisted_2, [
      _createVNode(_component_router_view)
    ]),
    (_ctx.showChrome)
      ? (_openBlock(), _createElementBlock("footer", _hoisted_3, [
          _createElementVNode("div", _hoisted_4, [
            _createElementVNode("span", null, "© " + _toDisplayString(_ctx.year) + " SwapU 换享", 1 /* TEXT */),
            _cache[0] || (_cache[0] = _createElementVNode("span", null, "校园二手交易平台 · 让闲置物品焕发新生", -1 /* CACHED */))
          ])
        ]))
      : _createCommentVNode("v-if", true)
  ]))
}